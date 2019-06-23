package nlip.github.vendingmachine;

import static java.lang.Math.min;
import static java.util.stream.Collectors.toMap;
import static nlip.github.vendingmachine.extensions.MathUtils.clamp;
import static nlip.github.vendingmachine.extensions.StreamUtils.repeat;

import static java.util.Optional.empty;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import nlip.github.vendingmachine.integration.SystemIntegration;
import nlip.github.vendingmachine.internalstate.CoinContainer;
import nlip.github.vendingmachine.internalstate.Holdings;
import nlip.github.vendingmachine.values.Denomination;
import nlip.github.vendingmachine.values.RegisterUpdate;

public final class Register {
	private final Map<Denomination, CoinContainer> coinContainers;
	private Holdings proposedChange;
	private RegisterUpdate proposedUpdate;

	public Register() {
		this.coinContainers = Denomination.descending().collect(toMap(x -> x, x -> new CoinContainer()));
		this.proposedChange = new Holdings();
	}

	public RegisterUpdate propose(RegisterUpdate registerUpdate) {
		var denomination = registerUpdate.getDenomination();
		var coinContainer = coinContainers.get(denomination);
		var adjustedProposedNumberOfCoins = clamp(
				registerUpdate.getNumberOfCoins(),
				-coinContainer.getNumberOfCoins(),
				coinContainer.getAvailableSpace());

		proposedUpdate = new RegisterUpdate(denomination, adjustedProposedNumberOfCoins);
		return proposedUpdate;
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
			repeat(-numberOfCoins, coinContainer::decrement);
		}
	}

	public boolean tryPutCoin(Denomination denomination) {
		var container = coinContainers.get(denomination);
		if (container.isAtCapacity()) {
			SystemIntegration.releaseChange(denomination);
			return false;
		}

		proposedChange.put(denomination, proposedChange.get(denomination) + 1);
		container.increment();
		
		SystemIntegration.showAmount(proposedChange.getValue());
		return true;
	}
	
	public boolean validatePotentialPurchase(BigDecimal price) {
		var newBalance = proposedChange.getValue().subtract(price);
		if (newBalance.signum() < 0) {
			SystemIntegration.showInsufficientFunds();
			return false;
		}

		if (proposeHoldings(newBalance).isEmpty()) {
			SystemIntegration.showInsufficientChange();
			return false;
		}
		return true;
	}

	public void purchase(BigDecimal price) {
		proposedChange = proposeHoldings(price)
				.orElseThrow(() -> new IllegalStateException("Cannot find holdings for purchase."));

	}

	public void returnCredit() {
		coinContainers.forEach((denomination, coinContainer) -> {
			int numberOfCoins = proposedChange.get(denomination);
			repeat(numberOfCoins, () -> {
				SystemIntegration.releaseChange(denomination);
				coinContainer.decrement();
			});
		});

		proposedChange.clear();
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
		
		if (holdings.getValue().compareTo(target) != 0) {
			return empty();
		}
		return Optional.of(holdings);
	}
}
