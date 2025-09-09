package com.crediya;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.google.gson.Gson;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class SqsMessageHandler implements RequestHandler<SQSEvent, Void> {

  private final SqsClient sqsClient = SqsClient.builder().region(Region.US_EAST_1).build();
  private final Gson gson = new Gson();
  private static final String RESULT_QUEUE_URL = System.getenv("RESULT_QUEUE_URL");
  private static final BigDecimal MAX_DEBT_RATIO = new BigDecimal("0.35");
  private static final int SALARY_MULTIPLIER_FOR_MANUAL_REVIEW = 5;

  @Override
  public Void handleRequest(SQSEvent sqsEvent, Context context) {
    LambdaLogger logger = context.getLogger();
    logger.log("Se ha recibido un lote de " + sqsEvent.getRecords().size() + " mensajes de SQS.");
    for (SQSEvent.SQSMessage message : sqsEvent.getRecords()) {
      try {
        String messageId = message.getMessageId();
        String messageBody = message.getBody();
        logger.log("Procesando Mensaje ID: " + messageId);
        logger.log("Cuerpo del Mensaje: " + messageBody);
        LoanValidationRequest request = gson.fromJson(messageBody, LoanValidationRequest.class);
        BigDecimal maxDebtCapacity = calculateMaximumDebtCapacity(request.getApplicantBaseSalary());
        BigDecimal currentMonthlyDebt = calculateCurrentMonthlyDebt(request.getActiveLoans(), logger);
        BigDecimal availableDebtCapacity = maxDebtCapacity.subtract(currentMonthlyDebt);
        BigDecimal newLoanMonthlyPayment = calculateMonthlyPayment(request.getNewLoan().getRequestedAmount(), request.getNewLoan().getMonthlyInterestRate(),
                                                                   request.getNewLoan().getRequestedTermMonths());
        String finalStatus;
        String reason;
        int statusId;
        if (newLoanMonthlyPayment.compareTo(availableDebtCapacity) <= 0) {
          finalStatus = "APROBADO";
          statusId = 2;
          reason = "La cuota del nuevo préstamo es asumible según la capacidad de endeudamiento disponible.";
          // Lógica adicional para revisión manual
          BigDecimal maxLoanAmountForAutoApproval = request.getApplicantBaseSalary().multiply(BigDecimal.valueOf(SALARY_MULTIPLIER_FOR_MANUAL_REVIEW));
          if (request.getNewLoan().getRequestedAmount().compareTo(maxLoanAmountForAutoApproval) > 0) {
            finalStatus = "REVISION MANUAL";
            statusId = 5;
            reason = "El monto del préstamo supera " + SALARY_MULTIPLIER_FOR_MANUAL_REVIEW + " veces el salario base del solicitante.";
          }
        } else {
          finalStatus = "RECHAZADO";
          statusId = 3;
          reason = "La cuota del nuevo préstamo supera la capacidad de endeudamiento disponible.";
        }
        logger.log("Decisión final para " + request.getApplicantEmail() + ": " + finalStatus);
        // Enviar resultado a la cola SQS
        sendResultToSqs(request.getApplicantEmail(), finalStatus, reason, statusId, logger);
      } catch (Exception e) {
        logger.log("ERROR al procesar el mensaje: " + message.getMessageId() + ". Error: " + e.getMessage());
        throw new RuntimeException("Fallo al procesar un mensaje, se reintentará el lote completo.", e);
      }
    }
    logger.log("Lote de mensajes procesado exitosamente.");
    return null;
  }

  private BigDecimal calculateMaximumDebtCapacity(BigDecimal totalIncome) {
    return totalIncome.multiply(MAX_DEBT_RATIO);
  }

  private BigDecimal calculateCurrentMonthlyDebt(List<Loan> loans, LambdaLogger logger) {
    if (loans == null || loans.isEmpty()) {
      logger.log("Sin prestamos activos");
      return BigDecimal.ZERO;
    }
    return loans
      .stream()
      .map(loan -> calculateMonthlyPayment(loan.getRequestedAmount(), loan.getMonthlyInterestRate(), loan.getRequestedTermMonths()))
      .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private BigDecimal calculateMonthlyPayment(BigDecimal principal, BigDecimal monthlyInterestRate, Integer termInMonths) {
    if (principal == null || monthlyInterestRate == null || termInMonths == null || termInMonths == 0) {
      return BigDecimal.ZERO;
    }
    // i(1+i)^n
    BigDecimal rateFactor = monthlyInterestRate.add(BigDecimal.ONE).pow(termInMonths);
    BigDecimal numerator = principal.multiply(monthlyInterestRate).multiply(rateFactor);
    // (1+i)^n - 1
    BigDecimal denominator = rateFactor.subtract(BigDecimal.ONE);
    if (denominator.compareTo(BigDecimal.ZERO) == 0) {
      // Evitar división por cero si la tasa es 0 o el plazo es muy corto
      return principal.divide(BigDecimal.valueOf(termInMonths), 2, RoundingMode.HALF_UP);
    }
    return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
  }

  private void sendResultToSqs(String applicantEmail, String status, String reason, Integer statusId, LambdaLogger logger) {
    ValidationResult result = new ValidationResult(applicantEmail, status, reason, statusId);
    String resultMessageBody = gson.toJson(result);
    logger.log("Resultado validacion nuevo prestamo" + resultMessageBody);
    try {
      SendMessageRequest sendMessageRequest = SendMessageRequest.builder().queueUrl(RESULT_QUEUE_URL).messageBody(resultMessageBody).build();
      sqsClient.sendMessage(sendMessageRequest);
      logger.log("Resultado enviado a SQS para el solicitante: " + applicantEmail);
    } catch (Exception e) {
      logger.log("ERROR al enviar el mensaje a SQS: " + e.getMessage());
      throw new RuntimeException("No se pudo enviar el mensaje de resultado a SQS.", e);
    }
  }

}
