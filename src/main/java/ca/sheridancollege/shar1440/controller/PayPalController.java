
package ca.sheridancollege.shar1440.controller;

import ca.sheridancollege.shar1440.services.PayPalService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/paypal")
@CrossOrigin(origins = "*")// Allows requests from any origin (important for frontend like Angular or JS fetch)
public class PayPalController {

    private final PayPalService payPalService;

    public PayPalController(PayPalService payPalService) {
        this.payPalService = payPalService;
    }

    
    
    // Endpoint: POST /api/paypal/create-order
    // Called from frontend when user clicks PayPal button
    @PostMapping("/create-order")
    public Map<String, Object> createOrder() {
        return payPalService.createOrder();
    }
    
    
    // Endpoint: POST /api/paypal/capture-order/{orderId}
    // Called after user approves payment in PayPal popup
    @PostMapping("/capture-order/{orderId}")
    public Map<String, Object> captureOrder(@PathVariable String orderId) {
        return payPalService.captureOrder(orderId);
    }
}




