/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package router;
 
import domain.Account;
import domain.Customer;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
 
public class AccountBuilder extends RouteBuilder {
    @Override
    public void configure() {
        
        // create HTTP endpoint for receiving messages via HTTP
        from("jetty:http://localhost:9000/api/account?enableCORS=true")
                // make message in-only so web browser doesn't have to wait on 
                //a non-existent response
                .setExchangePattern(ExchangePattern.InOnly)
                .to("jms:queue:retrieved_from_jetty");
        
        // Convert the JSON message into an Account object.
        from("jms:queue:retrieved_from_jetty")
                .unmarshal().json(JsonLibrary.Gson, Account.class)
                .to("jms:queue:account"); 
        
        /* 
         * Convert the Account object into a Vend-compatible Customer object.
         * id field is meant to be null at this point as the response will 
         * auto-generate one.
         */        
        from("jms:queue:account")
                .bean(Customer.class, "generateCustomer(${body})")
                .to("jms:queue:vend_customer");
        
        /* 
         * Request a Customer payload from the Vend endpoint and capture the 
         * result in a new queue.
         * https://docs.vendhq.com/reference/2/spec/customers/createcustomer
         */ 
        from("jms:queue:vend_customer")
                .removeHeaders("*")
                .setHeader("Authorization", constant("Bearer KiQSsELLtocyS2WDN5w5s_jYaBpXa0h2ex1mep1a"))
                .marshal().json(JsonLibrary.Gson)
                .setHeader(Exchange.CONTENT_TYPE).constant("application/json")
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .to("https://info303otago.vendhq.com/api/2.0/customers")
                .to("jms:queue:vend_response");
        
        // Extract the JSON response data from the 'data' field
        from("jms:queue:vend_response")
                .setBody().jsonpath("$.data")
                .marshal().json(JsonLibrary.Gson)
                .to("jms:queue:vend_extracted");
        
        // Unmarshal the data back into Customer
        from("jms:queue:vend_extracted")
                .unmarshal().json(JsonLibrary.Gson, Customer.class)
                .to("jms:queue:customer");
        
        // Convert customer back into Account, making sure ID field
        // is properly included this time
        from("jms:queue:customer")
                .bean(Account.class, "generateAccount(${body})")
                .to("jms:queue:account_send");
    
        // Marshal into JSON and send to the accounts service
        from("jms:queue:account_send")
                .removeHeaders("*")
                .marshal().json(JsonLibrary.Gson)
                .log("Account - ${body}")
                .setHeader(Exchange.CONTENT_TYPE).constant("application/json")
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .to("http://localhost:8086/api/accounts")
                .to("jms:queue:account_service_response");
                
    }
}
