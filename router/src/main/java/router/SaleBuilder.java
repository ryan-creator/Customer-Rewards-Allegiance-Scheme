package router;
 
import domain.Customer;
import domain.Sale;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
 
 
public class SaleBuilder extends RouteBuilder {
    @Override
    public void configure() {
        
//        from("imaps://outlook.office365.com?username=colry562@student.otago.ac.nz"
//        + "&password=" + getPassword("Enter your E-Mail password")
//        + "&searchTerm.subject=Vend:SaleUpdate"
//        + "&debugMode=true"  // set to true if you want to see the authentication details
//        + "&folderName=Vend")  // change to whatever folder your Vend messages end up in
//                .convertBodyTo(String.class)
//                .log("${body}")
//                .to("jms:queue:vend-new-sale");
 
        // Mock email service
        from("imap://localhost?username=test@localhost"
                + "&port=3143"
                + "&password=password"
                + "&consumer.delay=5000"
                + "&searchTerm.subject=Vend.SaleUpdate")
                .log("Found new E-Mail: ${body}")
                .to("jms:queue:messages");
       
        // Extract the customers current group and store it in a header
        from("jms:queue:messages")
                .setHeader("group").jsonpath("$.customer.customer_group_id")
                .to("jms:queue:group-header");
        
        // Create a sale object for the phase 1 sales service
        from("jms:queue:group-header")
                .unmarshal().json(JsonLibrary.Gson, Sale.class)
                .to("jms:queue:sale-objects");
        
        // POST a sale to the sale service
        from("jms:queue:sale-objects")
                .removeHeaders("*")
                .marshal().json(JsonLibrary.Gson)
                .setHeader(Exchange.CONTENT_TYPE).constant("application/json")
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .to("http://localhost:8081/api/sales")
                .to("jms:queue:sales-response");
        
        // Retrieve the customer’s sales summary from the phase 1 sales service.
        from("jms:queue:sales-response")
                .setProperty("id_for_summary").jsonpath("$.customer.id")
                .removeHeaders("*")
                .setHeader(Exchange.CONTENT_TYPE).constant("application/json")
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .toD("http://localhost:8081/api/sales/customer/${exchangeProperty.id_for_summary}/summary")
                .setProperty("customer_id").simple("${exchangeProperty.id_for_summary}")
                .to("jms:queue:summary");
        
        // Extract the customer’s calculated group from the summary.
        from("jms:queue:summary")
                .setProperty("customer_id").simple("${exchangeProperty.id_for_summary}")
                .setProperty("customer_group").jsonpath("$.group")
                .removeHeader("*")  
                .log("${body}")
                .setHeader(Exchange.CONTENT_TYPE).constant("application/json")
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .toD("http://localhost:8081/api/sales/customer/${exchangeProperty.customer_id}")
                .setProperty("old_group").jsonpath("$[0].customer.customer_group_id")
                .setProperty("group")
                .method(Customer.class,"checkGroup(${exchangeProperty.old_group}, ${exchangeProperty.customer_group})")
                .log("${body}")
                .to("jms:queue:send-to-endpoints"); 
        
//        from("jms:queue:send-to-endpoints")
//                //.marshal().json(JsonLibrary.Gson)
//                .removeHeaders("*")
//                .setHeader(Exchange.CONTENT_TYPE).constant("application/json")
//                .setHeader(Exchange.HTTP_METHOD, constant("PUT"))
//                .toD("http://localhost:8086/api/accounts/account/${exchangeProperty.customer-id}")
//                .log("${body}");
        
    }
    
    public static String getPassword(String prompt) {
        JPasswordField txtPasswd = new JPasswordField();
        int resp = JOptionPane.showConfirmDialog(null, txtPasswd, prompt,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (resp == JOptionPane.OK_OPTION) {
            String password = new String(txtPasswd.getPassword());
            return password;
        }
        return null;
    }
}