/**
 * Copyright (c) 2020-2021, Self XDSD Contributors
 * All rights reserved.
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"),
 * to read the Software only. Permission is hereby NOT GRANTED to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.selfxdsd.selfweb.api.output;

import com.selfxdsd.api.PaymentMethod;

import javax.json.Json;
import javax.json.JsonObject;

/**
 * PaymentMethod as JSON.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 0.0.4
 */
public final class JsonPaymentMethod extends AbstractJsonObject {

    /**
     * Ctor. Full JSON is included by default.
     * @param paymentMethod PaymentMethod to be converted to JsonObject.
     */
    public JsonPaymentMethod(final PaymentMethod paymentMethod) {
        this(paymentMethod, Boolean.TRUE);
    }

    /**
     * Ctor.
     * @param paymentMethod PaymentMethod to be converted to JsonObject.
     * @param fullJson Include the full JSON or not?
     */
    public JsonPaymentMethod(
        final PaymentMethod paymentMethod,
        final boolean fullJson
    ) {
        super(() -> {
            final JsonObject json;
            if(fullJson) {
                json = Json.createObjectBuilder()
                    .add(
                        "self",
                        Json.createObjectBuilder()
                            .add("paymentMethodId", paymentMethod.identifier())
                            .add("active", paymentMethod.active())
                            .build()
                    )
                    .add(
                        "stripe",
                        paymentMethod.json()
                    )
                    .build();
            } else {
                json = Json.createObjectBuilder()
                    .add(
                        "self",
                        Json.createObjectBuilder()
                            .add("paymentMethodId", paymentMethod.identifier())
                            .add("active", paymentMethod.active())
                            .build()
                    ).build();
            }
            return json;
        });
    }

}
