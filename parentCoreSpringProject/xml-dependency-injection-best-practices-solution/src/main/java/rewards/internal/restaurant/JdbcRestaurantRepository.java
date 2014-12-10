package rewards.internal.restaurant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;

import org.springframework.dao.EmptyResultDataAccessException;

import common.money.Percentage;

/**
 * Loads restaurants from a data source using the JDBC API.
 */
public class JdbcRestaurantRepository implements RestaurantRepository {

	private DataSource dataSource;
	private Map<String, Restaurant> restaurantCache;
	
	/**
	 * Sets the data source this repository will use to load restaurants.
	 * @param dataSource the data source
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public Restaurant findByMerchantNumber(String merchantNumber) {
		String sql = "select MERCHANT_NUMBER, NAME, BENEFIT_PERCENTAGE from T_RESTAURANT where MERCHANT_NUMBER = ?";
		Restaurant restaurant = null;
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = dataSource.getConnection();
			ps = conn.prepareStatement(sql);
			ps.setString(1, merchantNumber);
			rs = ps.executeQuery();
			advanceToNextRow(rs);
			restaurant = mapRestaurant(rs);
		} catch (SQLException e) {
			throw new RuntimeException("SQL exception occurred finding by merchant number", e);
		} finally {
			if (rs != null) {
				try {
					// Close to prevent database cursor exhaustion
					rs.close();
				} catch (SQLException ex) {
				}
			}
			if (ps != null) {
				try {
					// Close to prevent database cursor exhaustion
					ps.close();
				} catch (SQLException ex) {
				}
			}
			if (conn != null) {
				try {
					// Close to prevent database connection exhaustion
					conn.close();
				} catch (SQLException ex) {
				}
			}
		}
		return restaurant;
	}

	/**
	 * Maps a row returned from a query of T_RESTAURANT to a Restaurant object.
	 * @param rs the result set with its cursor positioned at the current row
	 */
	private Restaurant mapRestaurant(ResultSet rs) throws SQLException {
		// get the row column data
		String name = rs.getString("NAME");
		String number = rs.getString("MERCHANT_NUMBER");
		Percentage benefitPercentage = Percentage.valueOf(rs.getString("BENEFIT_PERCENTAGE"));
		// map to the object
		Restaurant restaurant = new Restaurant(number, name);
		restaurant.setBenefitPercentage(benefitPercentage);
		return restaurant;
	}

	/**
	 * Advances a ResultSet to the next row and throws an exception if there are no rows.
	 * @param rs the ResultSet to advance
	 * @throws EmptyResultDataAccessException if there is no next row
	 * @throws SQLException
	 */
	private void advanceToNextRow(ResultSet rs) throws EmptyResultDataAccessException, SQLException {
		if (!rs.next()) {
			throw new EmptyResultDataAccessException(1);
		}
	}
	
	@PreDestroy
	public 	void clearRestaurantCache() {
		restaurantCache.clear();
	}
	
	@PostConstruct
	void populateRestaurantCache() {
		restaurantCache = new HashMap<String, Restaurant>();
		String sql = "select MERCHANT_NUMBER, NAME, BENEFIT_PERCENTAGE from T_RESTAURANT";
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = dataSource.getConnection();
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				Restaurant restaurant = mapRestaurant(rs);
				// index the restaurant by its merchant number
				restaurantCache.put(restaurant.getNumber(), restaurant);
			}
		} catch (SQLException e) {
			throw new RuntimeException("SQL exception occurred finding by merchant number", e);
		} finally {
			if (rs != null) {
				try {
					// Close to prevent database cursor exhaustion
					rs.close();
				} catch (SQLException ex) {
				}
			}
			if (ps != null) {
				try {
					// Close to prevent database cursor exhaustion
					ps.close();
				} catch (SQLException ex) {
				}
			}
			if (conn != null) {
				try {
					// Close to prevent database connection exhaustion
					conn.close();
				} catch (SQLException ex) {
				}
			}
		}
	}

}