package com.mybank.account.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

public class AccountDetails {

	private long accountId; 
	
	@NotBlank(message = "userName must not be empty")
    @Pattern(regexp = "^[aA-zZ]\\w{5,29}$",
            message = "userName format validation failed")
    private String userName;

    @DecimalMin(value = "0.00", inclusive = true)
    @Digits(integer =15, fraction = 2, message = "Balance amount format incorrect")
    private BigDecimal balance;

    private int enabled;
    private String insertedBy;
    private OffsetDateTime insertedDate;
    private String updatedBy;
    private OffsetDateTime updatedDate;
	/**
	 * @return the accountId
	 */
	public long getAccountId() {
		return accountId;
	}
	/**
	 * @param accountId the accountId to set
	 */
	public void setAccountId(long accountId) {
		this.accountId = accountId;
	}
	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}
	/**
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}
	/**
	 * @return the balance
	 */
	public BigDecimal getBalance() {
		return balance;
	}
	/**
	 * @param balance the balance to set
	 */
	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}
	/**
	 * @return the enabled
	 */
	public int getEnabled() {
		return enabled;
	}
	/**
	 * @param enabled the enabled to set
	 */
	public void setEnabled(int enabled) {
		this.enabled = enabled;
	}
	/**
	 * @return the insertedBy
	 */
	public String getInsertedBy() {
		return insertedBy;
	}
	/**
	 * @param insertedBy the insertedBy to set
	 */
	public void setInsertedBy(String insertedBy) {
		this.insertedBy = insertedBy;
	}
	/**
	 * @return the insertedDate
	 */
	public OffsetDateTime getInsertedDate() {
		return insertedDate;
	}
	/**
	 * @param insertedDate the insertedDate to set
	 */
	public void setInsertedDate(OffsetDateTime insertedDate) {
		this.insertedDate = insertedDate;
	}
	/**
	 * @return the updatedBy
	 */
	public String getUpdatedBy() {
		return updatedBy;
	}
	/**
	 * @param updatedBy the updatedBy to set
	 */
	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}
	/**
	 * @return the updatedDate
	 */
	public OffsetDateTime getUpdatedDate() {
		return updatedDate;
	}
	/**
	 * @param updatedDate the updatedDate to set
	 */
	public void setUpdatedDate(OffsetDateTime updatedDate) {
		this.updatedDate = updatedDate;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (accountId ^ (accountId >>> 32));
		result = prime * result + ((balance == null) ? 0 : balance.hashCode());
		result = prime * result + enabled;
		result = prime * result + ((insertedBy == null) ? 0 : insertedBy.hashCode());
		result = prime * result + ((insertedDate == null) ? 0 : insertedDate.hashCode());
		result = prime * result + ((updatedBy == null) ? 0 : updatedBy.hashCode());
		result = prime * result + ((updatedDate == null) ? 0 : updatedDate.hashCode());
		result = prime * result + ((userName == null) ? 0 : userName.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AccountDetails other = (AccountDetails) obj;
		if (accountId != other.accountId)
			return false;
		if (balance == null) {
			if (other.balance != null)
				return false;
		} else if (!balance.equals(other.balance))
			return false;
		if (enabled != other.enabled)
			return false;
		if (insertedBy == null) {
			if (other.insertedBy != null)
				return false;
		} else if (!insertedBy.equals(other.insertedBy))
			return false;
		if (insertedDate == null) {
			if (other.insertedDate != null)
				return false;
		} else if (!insertedDate.equals(other.insertedDate))
			return false;
		if (updatedBy == null) {
			if (other.updatedBy != null)
				return false;
		} else if (!updatedBy.equals(other.updatedBy))
			return false;
		if (updatedDate == null) {
			if (other.updatedDate != null)
				return false;
		} else if (!updatedDate.equals(other.updatedDate))
			return false;
		if (userName == null) {
			if (other.userName != null)
				return false;
		} else if (!userName.equals(other.userName))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "AccountDetails [accountId=" + accountId + ", userName=" + userName + ", balance=" + balance
				+ ", enabled=" + enabled + ", insertedBy=" + insertedBy + ", insertedDate=" + insertedDate
				+ ", updatedBy=" + updatedBy + ", updatedDate=" + updatedDate + "]";
	}
    
    

    
}
