package com.example.carsharingapp.service;

import com.stripe.model.checkout.Session;
import java.math.BigDecimal;

public interface StripePaymentService {

    Session createSession(BigDecimal amount);
}
