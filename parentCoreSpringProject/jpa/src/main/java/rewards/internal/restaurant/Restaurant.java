package rewards.internal.restaurant;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Transient;

import rewards.Dining;
import rewards.internal.account.Account;

import common.money.MonetaryAmount;
import common.money.Percentage;

/**
 * A restaurant establishment in the network. Like AppleBee's.
 * 
 * Restaurants calculate how much benefit may be awarded to an account for
 * dining based on a availability policy and a benefit percentage.
 */
// TODO-07: Map this class using JPA Annotations
public class Restaurant {

	private Long entityId;

	private String number;

	private String name;

	// This is a non simple type - it needs mapping differently to the
	// fields above.
	private Percentage benefitPercentage;

	// DO NOT map this field. For now it is always set to AlwaysAvailable.
	// The bonus section later will redo this mapping.
	@Transient
	private BenefitAvailabilityPolicy benefitAvailabilityPolicy = AlwaysAvailable.INSTANCE;

	@SuppressWarnings("unused")
	private Restaurant() {
		//Needed by the JPA spec
	}

	/**
	 * Creates a new restaurant.
	 * 
	 * @param number
	 *            the restaurant's merchant number
	 * @param name
	 *            the name of the restaurant
	 */
	public Restaurant(String number, String name) {
		this.number = number;
		this.name = name;
	}

	/**
	 * Sets the percentage benefit to be awarded for eligible dining
	 * transactions.
	 * 
	 * @param benefitPercentage
	 *            the benefit percentage
	 */
	public void setBenefitPercentage(Percentage benefitPercentage) {
		this.benefitPercentage = benefitPercentage;
	}

	/**
	 * Sets the policy that determines if a dining by an account at this
	 * restaurant is eligible for benefit.
	 * 
	 * @param benefitAvailabilityPolicy
	 *            the benefit availability policy
	 */
	public void setBenefitAvailabilityPolicy(
			BenefitAvailabilityPolicy benefitAvailabilityPolicy) {
		this.benefitAvailabilityPolicy = benefitAvailabilityPolicy;
	}

	/**
	 * Returns the name of this restaurant.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the merchant number of this restaurant.
	 */
	public String getNumber() {
		return number;
	}

	/**
	 * Returns this restaurant's benefit percentage.
	 */
	public Percentage getBenefitPercentage() {
		return benefitPercentage;
	}

	/**
	 * Returns this restaurant's benefit availability policy.
	 */
	public BenefitAvailabilityPolicy getBenefitAvailabilityPolicy() {
		return benefitAvailabilityPolicy;
	}

	/**
	 * Returns the id for this restaurant.
	 */
	public Long getEntityId() {
		return entityId;
	}

	/**
	 * Calculate the benefit eligible to this account for dining at this
	 * restaurant.
	 * 
	 * @param account
	 *            the account that dined at this restaurant
	 * @param dining
	 *            a dining event that occurred
	 * @return the benefit amount eligible for reward
	 */
	public MonetaryAmount calculateBenefitFor(Account account, Dining dining) {
		if (benefitAvailabilityPolicy.isBenefitAvailableFor(account, dining)) {
			return dining.getAmount().multiplyBy(benefitPercentage);
		} else {
			return MonetaryAmount.zero();
		}
	}

	public String toString() {
		return "Number = '" + number + "', name = '" + name
				+ "', benefitPercentage = " + benefitPercentage
				+ ", benefitAvailabilityPolicy = " + benefitAvailabilityPolicy;
	}

	// Internal methods for JPA only - hence they are protected.
	// TODO-13: (BONUS) Uncomment the annotations on the next two methods and re-run the test 
	// Rerun the JpaRestaurantRepositoryTests and testNonParticipatingRestaurant should now fail. 
	// Fix the test to expect a NeverAvailable policy and rerun the test.
	/**
	 * Sets this restaurant's benefit availability policy from the code stored
	 * in the underlying column. This is a database specific accessor using the
	 * JPA 2 @Access annotation.
	 */
	//@Access(AccessType.PROPERTY)
	//@Column(name = "BENEFIT_AVAILABILITY_POLICY")
	protected void setDbBenefitAvailabilityPolicy(String policyCode) {
		if ("A".equals(policyCode)) {
			benefitAvailabilityPolicy = AlwaysAvailable.INSTANCE;
		} else if ("N".equals(policyCode)) {
			benefitAvailabilityPolicy = NeverAvailable.INSTANCE;
		} else {
			throw new IllegalArgumentException("Not a supported policy code "
					+ policyCode);
		}
	}

	/**
	 * Returns this restaurant's benefit availability policy code for storage in
	 * the underlying column. This is a database specific accessor using the JPA
	 * 2 @Access annotation.
	 */
	//@Access(AccessType.PROPERTY)
	//@Column(name = "BENEFIT_AVAILABILITY_POLICY")
	protected String getDbBenefitAvailabilityPolicy() {
		if (benefitAvailabilityPolicy == AlwaysAvailable.INSTANCE) {
			return "A";
		} else if (benefitAvailabilityPolicy == NeverAvailable.INSTANCE) {
			return "N";
		} else {
			throw new IllegalArgumentException("No policy code for "
					+ benefitAvailabilityPolicy.getClass());
		}
	}
}