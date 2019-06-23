package nlip.github.vendingmachine;

import static java.lang.Math.min;
import static java.util.Comparator.comparing;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toMap;
import static nlip.github.vendingmachine.extensions.MathUtils.clamp;
import static nlip.github.vendingmachine.extensions.StreamUtils.repeat;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import nlip.github.vendingmachine.integration.GuiIntegration;
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
		this.coinContainers = Denomination.stream()
				.collect(toMap(x -> x, __ -> new CoinContainer()));
		this.proposedChange = new Holdings();
	}

	public RegisterUpdate propose(RegisterUpdate registerUpdate) {
		var denomination = registerUpdate.getDenomination();
		var coinContainer = coinContainers.get(denomination);
		var adjustedProposedNumberOfCoins = clamp(
				registerUpdate.getNumberOfCoins(),
				-coinContainer.getNumberOfCoins(),
				coinContainer.getAvailableSpace());

		proposedUpdate = new RegisterUpdate(denomination,
				adjustedProposedNumberOfCoins);
		return proposedUpdate;
	}

	public void commitProposedUpdate() {
		if (proposedUpdate == null) {
			return;
		}
		var numberOfCoins = proposedUpdate.getNumberOfCoins();
		var denomination = proposedUpdate.getDenomination();
		var coinContainer = coinContainers.get(denomination);

		if (numberOfCoins > 0) {
			repeat(numberOfCoins, coinContainer::increment);
		} else {
			repeat(-numberOfCoins, coinContainer::decrement);
		}

		GuiIntegration.renderCoins(denomination,
				coinContainer.getNumberOfCoins());
	}

	public boolean tryPutCoin(Denomination denomination) {
		var container = coinContainers.get(denomination);
		if (container.isAtCapacity()) {
			SystemIntegration.releaseCoin(denomination);
			showCurrentBalance();
			return false;
		}

		proposedChange.put(denomination, proposedChange.get(denomination) + 1);
		container.increment();

		showCurrentBalance();
		return true;
	}

	public boolean tryPurchase(BigDecimal price) {
		var newBalance = proposedChange.getValue().subtract(price);
		if (newBalance.signum() < 0) {
			SystemIntegration.showInsufficientFunds();
			return false;
		}

		var maybeNewProposedHoldings = proposeHoldings(newBalance);
		if (maybeNewProposedHoldings.isEmpty()) {
			SystemIntegration.showInsufficientChange();
			return false;
		}

		proposedChange = maybeNewProposedHoldings.get();
		showCurrentBalance();
		return true;
	}

	public void releaseCredit() {
		coinContainers.forEach((denomination, coinContainer) -> {
			int numberOfCoins = proposedChange.get(denomination);
			repeat(numberOfCoins, () -> {
				coinContainer.decrement();
				SystemIntegration.releaseCoin(denomination);
			});
		});

		proposedChange.clear();
		showCurrentBalance();
	}

	private void showCurrentBalance() {
		SystemIntegration.showAmount(proposedChange.getValue());
		coinContainers.forEach((denomination, coinContainer) -> GuiIntegration
				.renderCoins(denomination, coinContainer.getNumberOfCoins()));
	}

	private Optional<Holdings> proposeHoldings(BigDecimal target) {
		var holdings = new Holdings();
		// TODO: Describe algo
		Denomination.stream()
				.sorted(comparing(Denomination::getValue).reversed())
				.forEach(denomination -> {
					var remainder = target.subtract(holdings.getValue());
					int availableCoins = coinContainers.get(denomination)
							.getNumberOfCoins();
					int numberOfCoins = min(availableCoins,
							remainder
									.divideToIntegralValue(
											denomination.getValue())
									.intValueExact());
					holdings.put(denomination, numberOfCoins);
				});

		if (holdings.getValue().compareTo(target) != 0) {
			return empty();
		}
		return Optional.of(holdings);
	}
}
