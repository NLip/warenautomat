package state;

import static extensions.BigDecimalUtils.eq;
import static extensions.BigDecimalUtils.isNegative;
import static extensions.StreamUtils.repeat;
import static java.lang.Math.abs;
import static java.lang.Math.min;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toMap;
import static values.CustomerProblem.INSUFFICIENT_BALANCE;
import static values.CustomerProblem.INSUFFICIENT_CHANGE;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import exceptions.InvalidOperationException;
import values.CustomerProblem;
import values.Denomination;
import values.RegisterUpdate;
import warenautomat.SystemSoftware;

public final class Register {
	private final Map<Denomination, CoinContainer> coinContainers;
	private Holdings proposedChange;
	private RegisterUpdate proposedUpdate;

	public Register() {
		coinContainers = Denomination.descending().collect(toMap(x -> x, x -> new CoinContainer()));
		proposedChange = new Holdings();
	}

	public RegisterUpdate propose(RegisterUpdate registerUpdate) {
		var denomination = registerUpdate.getDenomination();
		var coinContainer = coinContainers.get(denomination);
		var adjustedProposedNumberOfCoins = retrievePossibleChange(coinContainer, registerUpdate.getNumberOfCoins());

		proposedUpdate = new RegisterUpdate(denomination, adjustedProposedNumberOfCoins);
		return proposedUpdate;
	}

	private int retrievePossibleChange(CoinContainer coinContainer, int desiredChange) {
		return desiredChange > 0 ? min(desiredChange, coinContainer.getAvailableSpace())
				: -min(abs(desiredChange), coinContainer.getNumberOfCoins());

	}

	public void commitProposedUpdate() {
		if (proposedUpdate == null) {
			return;
		}
		var numberOfCoins = proposedUpdate.getNumberOfCoins();
		var coinContainer = coinContainers.get(proposedUpdate.getDenomination());

		if (numberOfCoins > 0) {
			repeat(numberOfCoins, coinContainer::increment);
		} else {
			repeat(numberOfCoins, coinContainer::decrement);
		}
	}

	public boolean tryPutCoin(Denomination denomination) {
		var container = coinContainers.get(denomination);
		if (container.isAtCapacity()) {
			releaseCoin(denomination);
			return false;
		}

		proposedChange.increment(denomination);
		container.increment();
		return true;
	}

	public Optional<CustomerProblem> proposePurchase(BigDecimal price) {
		var newBalance = computeCredit().subtract(price);
		if (isNegative(newBalance)) {
			return Optional.of(INSUFFICIENT_BALANCE);
		}

		if (proposeHoldings(newBalance).isEmpty()) {
			return Optional.of(INSUFFICIENT_CHANGE);
		}
		return empty();
	}

	public void purchase(BigDecimal price) {
		proposedChange = proposeHoldings(price)
				.orElseThrow(() -> new InvalidOperationException("Cannot find holdings for purchase."));
	}

	public void returnCredit() {
		coinContainers.forEach(((denomination, coinContainer) -> {
			int numberOfCoins = proposedChange.get(denomination);
			if (numberOfCoins != 0) {
				repeat(numberOfCoins).forEach(__ -> {
					releaseCoin(denomination);
					coinContainer.decrement();
				});
			}
		}));

		proposedChange = new Holdings();
	}

	private void releaseCoin(Denomination denomination) {
		SystemSoftware.auswerfenWechselGeld(denomination.getValue().doubleValue());
	}

	private BigDecimal computeCredit() {
		return proposedChange.getValue();
	}

	private Optional<Holdings> proposeHoldings(BigDecimal target) {
		var holdings = new Holdings();
		// TODO: Describe algo
		Denomination.descending().forEach(denomination -> {
			var remainder = target.subtract(holdings.getValue());
			int availableCoins = coinContainers.get(denomination).getNumberOfCoins();
			int numberOfCoins = min(availableCoins,
					remainder.divideToIntegralValue(denomination.getValue()).intValueExact());
			holdings.put(denomination, numberOfCoins);
		});

		return Optional.of(holdings).filter(h -> eq(h.getValue(), target));
	}
}
