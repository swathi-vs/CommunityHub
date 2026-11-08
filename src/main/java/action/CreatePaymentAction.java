package action;

import model.Payment;
import services.PaymentService;

public class CreatePaymentAction extends BaseAction {

	private static final long serialVersionUID = 1L;

	private Payment payment = new Payment();

	public Payment getPayment() {
		return payment;
	}

	public void setPayment(Payment payment) {
		this.payment = payment;
	}

	@Override
	public void validate() {
		if (payment.getAmount() == null || payment.getAmount() <= 0)
			addFieldError("payment.amount", "Amount required");
		if (!java.util.Arrays.asList("cash", "upi", "card", "bank_transfer", "cheque", "other").contains(payment.getMethod()))
			addFieldError("payment.method", "Method must be cash, upi, card, bank_transfer, cheque or other");
	}

	@Override
	public String execute() {
		payment.setRecordedUserId(currentUserId());
		return respond(PaymentService.createPayment(payment));
	}
}
