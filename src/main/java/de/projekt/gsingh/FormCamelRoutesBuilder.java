package de.projekt.gsingh;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;

import javax.enterprise.context.ApplicationScoped;

/***
 * A bean responsible for handling Camel Routes.
 * @author gsingh
 */

@ApplicationScoped
public class FormCamelRoutesBuilder extends RouteBuilder {

    @Override
    public void configure() {
        from("platform-http:/?httpMethodRestrict=POST")
                .id("receiving-http-route")
                .choice()
                .when(header(Exchange.HTTP_METHOD).isEqualTo(constant("POST")))
                .setHeader(Exchange.HTTP_QUERY,simple("name=${header.name}"))
                .setHeader(Exchange.HTTP_QUERY,simple("email=${header.email}"))
                .setHeader(Exchange.HTTP_QUERY,simple("message=${header.message}"))

                .log("Received POST Submission")
                .to("direct:processing-template");

        from("direct:processing-template")
                .id("processing-form-data")
                .setHeader("timestamp", simple("${date:now:dd/mm/yyyy  'Zeit'  HH:mm:ss }"))
                .setHeader("name",simple("${header.name}"))
                .setHeader("email",simple("${header.email}"))
                .setHeader("message",simple("${header.message}"))

                .to("velocity:email.vm")

                .removeHeaders("*", "email", "timestamp")

                .setHeader("To", simple("{{mail.to}}"))
                .setHeader("From", simple("{{mail.from}}"))
                .setHeader("Reply-To", simple("${header.email}"))
                .setHeader("Subject", simple("{{mail.subject}}"))

                .to("{{smtp.uri}}")

                .log("Sent email to {{mail.to}}")

                .removeHeaders("*")
                .setHeader("Location", simple("{{redirect.success}}"))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(303)) // Redirect 303 See Other after form submission
                .transform(constant(""));

    }
}
