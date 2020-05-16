package domain;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.LinkedHashMap;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Customer implements Serializable {
    
    // Regular customer group id
    private final String regularCustomer = "0afa8de1-147c-11e8-edec-2b197906d816";
    // ViP customer group id
    private final String vipCustomer = "0afa8de1-147c-11e8-edec-201e0f00872c";
    
    private String id;

    @SerializedName("customer_group_id")
    private String group;

    @SerializedName("first_name")
    private String firstName;

    @SerializedName("last_name")
    private String lastName;

    @SerializedName("customer_code")
    private String customerCode;

    private String email;
        
    public Customer generateCustomer(Account account) {
        Customer cust = new Customer();  
        cust.setGroup(regularCustomer);
        cust.setCustomerCode(account.getUsername());
        cust.setFirstName(account.getFirstName());
        cust.setLastName(account.getLastName());
        cust.setEmail(account.getEmail());
        return cust;
    }
    
    /**
     *
     * @param id
     * @param fname
     * @param lname
     * @param email
     * @param group
     * @param uname
     * @return
     */
    public Customer generateCustomer(String id, String fname, String lname, String email, String group, String uname){
        Customer cust = new Customer();
        cust.setId(id);
        cust.setFirstName(fname);
        cust.setLastName(lname);
        cust.setEmail(email);
        cust.setGroup(group);
        cust.setCustomerCode(uname);
        return cust;
    }
    
    public Customer editGroup(LinkedHashMap cust, String group) {
            Customer customer = new Customer();
            customer.setId(cust.get("id").toString());
            customer.setCustomerCode(cust.get("customer_code").toString());
            customer.setFirstName(cust.get("first_name").toString());
            customer.setLastName(cust.get("last_name").toString());
            customer.setEmail(cust.get("email").toString());
            customer.setGroup(group);
            return customer;
        }
    
    public String checkGroup(String groupID, String group){
        String newGroup;
        
        if(group.equals("VIP Customer")){
            newGroup = vipCustomer;
        } else {
            newGroup = regularCustomer;
        }
        
        if(newGroup.equals(groupID)){
            return groupID;
        } else {
            return newGroup;
        }
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public Customer() {
    }

    public String getId() {
            return id;
    }

    public void setId(String id) {
            this.id = id;
    }

    public String getEmail() {
            return email;
    }

    public void setEmail(String email) {
            this.email = email;
    }

    public String getGroup() {
            return group;
    }

    public void setGroup(String group) {
            this.group = group;
    }

    @Override
    public String toString() {
            return "Customer{" + "id=" + id + ", group=" + group + ", email=" + email +  ", firstName=" + firstName + ", lastName=" + lastName + ", customerCode=" + customerCode + '}';
    }

}
