package rewards.internal;

import rewards.AccountContribution;
import rewards.Dining;
import rewards.RewardConfirmation;
import rewards.RewardNetwork;
import rewards.internal.account.Account;
import rewards.internal.account.AccountRepository;
import rewards.internal.restaurant.Restaurant;
import rewards.internal.restaurant.RestaurantRepository;
import rewards.internal.reward.RewardRepository;

import common.money.MonetaryAmount;

/**
 * Rewards an Account for Dining at a Restaurant.
 * 
 * The sole Reward Network implementation. This object is an application-layer service responsible for coordinating with
 * the domain-layer to carry out the process of rewarding benefits to accounts for dining.
 * 
 * Said in other words, this class implements the "reward account for dining" use case.
 */

/* TODO-02: Configure Dependency Injection for all 3 dependencies (and decide if you should do it at the field level or constructor level) */

/* TODO-11: Annotate the class with appropriate stereotype annotation to indicate that the class is an auto scan component. 
			After making the changes, execute the test and see that it passes. */


public class RewardNetworkImpl implements RewardNetwork {

	private AccountRepository accountRepository;

	private RestaurantRepository restaurantRepository;

	private RewardRepository rewardRepository;

	/**
	 * Creates a new reward network.
	 * @param accountRepository the repository for loading accounts to reward
	 * @param restaurantRepository the repository for loading restaurants that determine how much to reward
	 * @param rewardRepository the repository for recording a record of successful reward transactions
	 */
	
	public RewardNetworkImpl(AccountRepository accountRepository, RestaurantRepository restaurantRepository,
			RewardRepository rewardRepository) {
		this.accountRepository = accountRepository;
		this.restaurantRepository = restaurantRepository;
		this.rewardRepository = rewardRepository;
	}

	public RewardConfirmation rewardAccountFor(Dining dining) {
		Account account = accountRepository.findByCreditCard(dining.getCreditCardNumber());
		Restaurant restaurant = restaurantRepository.findByMerchantNumber(dining.getMerchantNumber());
		MonetaryAmount amount = restaurant.calculateBenefitFor(account, dining);
		AccountContribution contribution = account.makeContribution(amount);
		accountRepository.updateBeneficiaries(account);
		return rewardRepository.confirmReward(contribution, dining);
	}
}