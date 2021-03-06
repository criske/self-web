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
package com.selfxdsd.selfweb.api;

import com.selfxdsd.api.*;
import com.selfxdsd.selfweb.api.input.ContractInput;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import java.io.StringReader;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

/**
 * Unit tests for {@link ContractsApi}.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 0.0.1
 * @checkstyle ExecutableStatementCount (1000 lines)
 */
public final class ContractsApiTestCase {

    /**
     * ContractsApi.contracts(...) returns Project Contracts
     * if Project is owned directly by the authenticated user (personal repo).
     */
    @Test
    public void fetchesOwnedProjectContracts(){
        final Project project = this.mockActiveProject(
            "mihai", "mihai", "test"
        );
        final Projects projects = Mockito.mock(Projects.class);
        Mockito.when(projects.getProjectById(
            "mihai/test", "github"
        )).thenReturn(project);

        final Contracts contracts = this.mockContracts(
            this.mockContract(
                new Contract.Id("mihai/test",
                    "vlad", "github", "DEV"),
                project,
                BigDecimal.valueOf(500),
                BigDecimal.valueOf(10000)
            )
        );
        Mockito.when(project.contracts()).thenReturn(contracts);

        final User user = Mockito.mock(User.class);

        Mockito.when(user.username()).thenReturn("mihai");
        final Provider provider = Mockito.mock(Provider.class);
        Mockito.when(provider.name()).thenReturn("github");
        Mockito.when(user.provider()).thenReturn(provider);
        Mockito.when(user.projects()).thenReturn(projects);

        final ContractsApi api = new ContractsApi(user);

        ResponseEntity<String> resp = api.contracts("mihai", "test");
        MatcherAssert.assertThat(
            resp.getStatusCode(),
            Matchers.is(HttpStatus.OK)
        );
        final JsonArray json = Json.createReader(
            new StringReader(Objects.requireNonNull(resp.getBody()))
        ).readArray();
        MatcherAssert.assertThat(
            json,
            Matchers.equalTo(Json.createArrayBuilder()
                .add(Json.createObjectBuilder()
                    .add("id", Json.createObjectBuilder()
                        .add("repoFullName", "mihai/test")
                        .add("contributorUsername", "vlad")
                        .add("provider", "github")
                        .add("role", "DEV")
                        .build())
                    .add(
                        "hourlyRate",
                        NumberFormat
                            .getCurrencyInstance(Locale.GERMANY)
                            .format(5)
                    ).add(
                        "value",
                        NumberFormat
                            .getCurrencyInstance(Locale.GERMANY)
                            .format(100)
                    ).add(
                        "revenue",
                        NumberFormat
                            .getCurrencyInstance(Locale.GERMANY)
                            .format(90)
                    )
                    .add("markedForRemoval", "null")
                    .build())
                .build())
        );

    }

    /**
     * Returns an empty json array if project not found.
     */
    @Test
    public void fetchesEmptyContractsIfProjectNotFound(){
        final User user = Mockito.mock(User.class);
        Mockito.when(user.username()).thenReturn("mihai");
        final Provider provider = Mockito.mock(Provider.class);
        Mockito.when(provider.name()).thenReturn("github");
        Mockito.when(user.provider()).thenReturn(provider);
        Mockito.when(user.projects()).thenReturn(Mockito.mock(Projects.class));

        final ContractsApi api = new ContractsApi(user);

        ResponseEntity<String> resp = api.contracts("mihai", "test");
        MatcherAssert.assertThat(
            resp.getStatusCode(),
            Matchers.is(HttpStatus.OK)
        );
        final JsonArray json = Json.createReader(
            new StringReader(Objects.requireNonNull(resp.getBody()))
        ).readArray();
        MatcherAssert.assertThat(
            json,
            Matchers.emptyIterable()
        );
    }

    /**
     * Adds a new Contributor Contract.
     */
    @Test
    public void addsNewContributorContract(){
        final User user = Mockito.mock(User.class);
        final Provider provider = Mockito.mock(Provider.class);
        final Projects projects = Mockito.mock(Projects.class);
        final Project project = this.mockActiveProject("mihai",
            "mihai", "test");
        final Contracts contracts = Mockito.mock(Contracts.class);

        Mockito.when(user.username()).thenReturn("mihai");
        Mockito.when(provider.name()).thenReturn("github");
        Mockito.when(user.provider()).thenReturn(provider);
        Mockito.when(user.projects()).thenReturn(projects);
        Mockito.when(projects
            .getProjectById(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(project);
        Mockito.when(project.contracts()).thenReturn(contracts);
        Mockito.when(contracts.addContract(Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.any(BigDecimal.class),
            Mockito.anyString()))
            .thenAnswer(inv -> {
                final String role = inv.getArgument(4);
                final Contract.Id id = new Contract.Id(
                    inv.getArgument(0),
                    inv.getArgument(1),
                    inv.getArgument(2),
                    role
                );
                final BigDecimal hourlyRate = inv.getArgument(3);
                return this.mockContract(id, project, hourlyRate,
                    BigDecimal.valueOf(25));
            });

        final ContractsApi api = new ContractsApi(user);

        final ContractInput input = new ContractInput();
        input.setUsername("john");
        input.setHourlyRate(16.33);
        input.setRole(Contract.Roles.DEV);

        MatcherAssert.assertThat(api
                .contracts("mihai", "test", input).getStatusCode(),
            Matchers.is(HttpStatus.CREATED));
        Mockito.verify(contracts)
            .addContract(
                "mihai/test",
                "john",
                "github",
                BigDecimal.valueOf(1633).setScale(2),
                "DEV"
            );
    }

    /**
     * Returns HttpStatus.PRECONDITION_FAILED if contract was not created.
     */
    @Test
    public void contractIsNotAdded(){
        final User user = Mockito.mock(User.class);
        final Provider provider = Mockito.mock(Provider.class);
        final Projects projects = Mockito.mock(Projects.class);
        final Project project = this.mockActiveProject("mihai",
            "mihai", "test");
        final Contracts contracts = Mockito.mock(Contracts.class);

        Mockito.when(user.username()).thenReturn("mihai");
        Mockito.when(provider.name()).thenReturn("github");
        Mockito.when(user.provider()).thenReturn(provider);
        Mockito.when(user.projects()).thenReturn(projects);
        Mockito.when(projects
            .getProjectById(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(project);
        Mockito.when(project.contracts()).thenReturn(contracts);
        Mockito.when(contracts.addContract(Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.any(BigDecimal.class),
            Mockito.anyString()))
            .thenThrow(new IllegalStateException("Contract not created!"));

        final ContractsApi api = new ContractsApi(user);

        final ContractInput input = new ContractInput();
        input.setUsername("john");
        input.setHourlyRate(10);
        input.setRole(Contract.Roles.DEV);
        MatcherAssert.assertThat(api
                .contracts("mihai", "test", input).getStatusCode(),
            Matchers.is(HttpStatus.PRECONDITION_FAILED));

    }

    /**
     * Returns HttpStatus.PRECONDITION_FAILED if contract was not created
     * due to project not found.
     */
    @Test
    public void contractIsNotAddedIfProjectNotFound(){
        final User user = Mockito.mock(User.class);
        final Provider provider = Mockito.mock(Provider.class);
        final Projects projects = Mockito.mock(Projects.class);
        final Contracts contracts = Mockito.mock(Contracts.class);

        Mockito.when(user.username()).thenReturn("mihai");
        Mockito.when(provider.name()).thenReturn("github");
        Mockito.when(user.provider()).thenReturn(provider);
        Mockito.when(user.projects()).thenReturn(projects);

        Mockito.when(contracts.addContract(Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.any(BigDecimal.class),
            Mockito.anyString()))
            .thenThrow(new IllegalStateException("Contract not created!"));

        final ContractsApi api = new ContractsApi(user);

        final ContractInput input = new ContractInput();
        input.setUsername("john");
        input.setHourlyRate(10);
        input.setRole(Contract.Roles.DEV);
        MatcherAssert.assertThat(api
                .contracts("mihai", "test", input).getStatusCode(),
            Matchers.is(HttpStatus.PRECONDITION_FAILED));

    }

    /**
     * ContractsApi.restoreContract(...) ignores restoring if Contract is not
     * marked for removal.
     */
    @Test
    public void restoreIsIgnoreIfContractNotMarked(){
        final User user = Mockito.mock(User.class);
        final Provider provider = Mockito.mock(Provider.class);
        final Projects projects = Mockito.mock(Projects.class);
        final Project project = this.mockActiveProject("mihai",
            "mihai", "test");
        final Contracts contracts = Mockito.mock(Contracts.class);
        final Contract.Id contractId = new Contract.Id(
            "mihai/test",
            "john",
            "github",
            "DEV"
        );
        final Contract contract = this.mockContract(
            contractId,
            project,
            BigDecimal.TEN,
            BigDecimal.TEN
        );

        Mockito.when(user.username()).thenReturn("mihai");
        Mockito.when(provider.name()).thenReturn("github");
        Mockito.when(user.provider()).thenReturn(provider);
        Mockito.when(user.projects()).thenReturn(projects);
        Mockito.when(projects.getProjectById("mihai/test", "github"))
            .thenReturn(project);
        Mockito.when(project.contracts()).thenReturn(contracts);
        Mockito.when(contracts.findById(contractId)).thenReturn(contract);

        final ContractsApi api = new ContractsApi(user);

        ResponseEntity<String> resp = api
            .restoreContract("mihai", "test", "john", "DEV");
        MatcherAssert.assertThat(resp.getStatusCode(),
            Matchers.is(HttpStatus.NO_CONTENT));
    }

    /**
     * ContractsApi.restoreContract(...) ignores restoring if Contract is not
     * found.
     */
    @Test
    public void restoreIsIgnoredIfContractNotFound(){
        final User user = Mockito.mock(User.class);
        final Provider provider = Mockito.mock(Provider.class);
        final Projects projects = Mockito.mock(Projects.class);
        final Project project = this.mockActiveProject("mihai",
            "mihai", "test");
        final Contracts contracts = Mockito.mock(Contracts.class);

        Mockito.when(user.username()).thenReturn("mihai");
        Mockito.when(provider.name()).thenReturn("github");
        Mockito.when(user.provider()).thenReturn(provider);
        Mockito.when(user.projects()).thenReturn(projects);
        Mockito.when(projects.getProjectById("mihai/test", "github"))
            .thenReturn(project);
        Mockito.when(project.contracts()).thenReturn(contracts);

        final ContractsApi api = new ContractsApi(user);

        ResponseEntity<String> resp = api
            .restoreContract("mihai", "test", "john", "DEV");
        MatcherAssert.assertThat(resp.getStatusCode(),
            Matchers.is(HttpStatus.NO_CONTENT));
    }

    /**
     * ContractsApi.restoreContract(...) ignores restoring if Project is not
     * found.
     */
    @Test
    public void restoreIsIgnoredIfProjectNotFound(){
        final User user = Mockito.mock(User.class);
        final Provider provider = Mockito.mock(Provider.class);
        final Projects projects = Mockito.mock(Projects.class);

        Mockito.when(user.username()).thenReturn("mihai");
        Mockito.when(provider.name()).thenReturn("github");
        Mockito.when(user.provider()).thenReturn(provider);
        Mockito.when(user.projects()).thenReturn(projects);

        final ContractsApi api = new ContractsApi(user);

        ResponseEntity<String> resp = api
            .restoreContract("mihai", "test", "john", "DEV");
        MatcherAssert.assertThat(resp.getStatusCode(),
            Matchers.is(HttpStatus.NO_CONTENT));
    }

    /**
     * ContractsApi.payInvoice(...) is successfully paid.
     */
    @Test
    public void invoicePaymentIsOk(){
        final User user = Mockito.mock(User.class);
        final Provider provider = Mockito.mock(Provider.class);
        final Projects projects = Mockito.mock(Projects.class);
        final Project project = this.mockActiveProject("mihai",
            "mihai", "test");
        final Contracts contracts = Mockito.mock(Contracts.class);
        final Contract.Id contractId = new Contract.Id(
            "mihai/test",
            "john",
            "github",
            "DEV"
        );
        final Contract contract = this.mockContract(
            contractId,
            project,
            BigDecimal.TEN,
            BigDecimal.TEN
        );
        final Invoice invoice = this.mockInvoice(1, false,
            null);
        final Invoice active = this.mockInvoice(2, false,
            this.mockPayment(Payment.Status.SUCCESSFUL, ""));
        final Invoices invoices = Mockito.mock(Invoices.class);
        final Wallet wallet = Mockito.mock(Wallet.class);
        final Wallets wallets = Mockito.mock(Wallets.class);
        final Payment payment = this.mockPayment(Payment.Status.SUCCESSFUL, "");

        Mockito.when(user.username()).thenReturn("mihai");
        Mockito.when(provider.name()).thenReturn("github");
        Mockito.when(user.provider()).thenReturn(provider);
        Mockito.when(user.projects()).thenReturn(projects);
        Mockito.when(projects.getProjectById("mihai/test", "github"))
            .thenReturn(project);
        Mockito.when(project.contracts()).thenReturn(contracts);
        Mockito.when(contracts.findById(contractId)).thenReturn(contract);

        Mockito.when(contract.invoices()).thenReturn(invoices);
        Mockito.when(invoices.getById(1)).thenReturn(invoice);
        Mockito.when(invoices.active()).thenReturn(active);
        Mockito.when(project.wallets()).thenReturn(wallets);
        Mockito.when(wallets.active()).thenReturn(wallet);
        Mockito.when(wallet.pay(invoice)).thenReturn(payment);

        final ContractsApi api = new ContractsApi(user);

        final ResponseEntity<String> resp =
            api.payInvoice("mihai", "test", "john", 1, "DEV");
        final JsonObject json = Json.createReader(
            new StringReader(Objects.requireNonNull(resp.getBody()))
        ).readObject();
        MatcherAssert.assertThat(resp.getStatusCode(),
            Matchers.equalTo(HttpStatus.OK));
        MatcherAssert.assertThat(
            json.getInt("paid"),
            Matchers.equalTo(1)
        );
        MatcherAssert.assertThat(
            json.getJsonObject("payment")
                .getString("transactionId"),
            Matchers.equalTo("tx_123")
        );
        MatcherAssert.assertThat(
            json.getJsonObject("payment")
                .getString("status"),
            Matchers.equalTo(Payment.Status.SUCCESSFUL)
        );
        MatcherAssert.assertThat(
            json.getJsonObject("active")
                .getInt("id"),
            Matchers.equalTo(2)
        );
    }

    /**
     * ContractsApi.payInvoice(...) fails with either FAILED or ERROR status.
     */
    @Test
    public void invoicePaymentFails(){
        final User user = Mockito.mock(User.class);
        final Provider provider = Mockito.mock(Provider.class);
        final Projects projects = Mockito.mock(Projects.class);
        final Project project = this.mockActiveProject("mihai",
            "mihai", "test");
        final Contracts contracts = Mockito.mock(Contracts.class);
        final Contract.Id contractId = new Contract.Id(
            "mihai/test",
            "john",
            "github",
            "DEV"
        );
        final Contract contract = this.mockContract(
            contractId,
            project,
            BigDecimal.TEN,
            BigDecimal.TEN
        );
        final Invoice invoice = this.mockInvoice(1, false,
            null);
        final Invoice active = this.mockInvoice(2, false,
            this.mockPayment(Payment.Status.SUCCESSFUL, ""));
        final Invoices invoices = Mockito.mock(Invoices.class);
        final Wallet wallet = Mockito.mock(Wallet.class);
        final Wallets wallets = Mockito.mock(Wallets.class);
        final Payment payment = this.mockPayment(Payment.Status.FAILED,
            "failed");

        Mockito.when(user.username()).thenReturn("mihai");
        Mockito.when(provider.name()).thenReturn("github");
        Mockito.when(user.provider()).thenReturn(provider);
        Mockito.when(user.projects()).thenReturn(projects);
        Mockito.when(projects.getProjectById("mihai/test", "github"))
            .thenReturn(project);
        Mockito.when(project.contracts()).thenReturn(contracts);
        Mockito.when(contracts.findById(contractId)).thenReturn(contract);

        Mockito.when(contract.invoices()).thenReturn(invoices);
        Mockito.when(invoices.getById(1)).thenReturn(invoice);
        Mockito.when(invoices.active()).thenReturn(active);
        Mockito.when(project.wallets()).thenReturn(wallets);
        Mockito.when(wallets.active()).thenReturn(wallet);
        Mockito.when(wallet.pay(invoice)).thenReturn(payment);

        final ContractsApi api = new ContractsApi(user);

        final ResponseEntity<String> resp =
            api.payInvoice("mihai", "test", "john", 1, "DEV");
        final JsonObject json = Json.createReader(
            new StringReader(Objects.requireNonNull(resp.getBody()))
        ).readObject();
        MatcherAssert.assertThat(resp.getStatusCode(),
            Matchers.equalTo(HttpStatus.OK));
        MatcherAssert.assertThat(
            json.getInt("paid"),
            Matchers.equalTo(1)
        );
        MatcherAssert.assertThat(
            json.getJsonObject("payment")
                .getString("status"),
            Matchers.equalTo(Payment.Status.FAILED)
        );
        MatcherAssert.assertThat(
            json.getJsonObject("payment")
                .getString("failReason"),
            Matchers.equalTo("failed")
        );
        MatcherAssert.assertThat(
            json.getJsonObject("payment")
                .getString("transactionId"),
            Matchers.equalTo("tx_123")
        );
        MatcherAssert.assertThat(
            json.getJsonObject("active")
                .getInt("id"),
            Matchers.equalTo(2)
        );
    }

    /**
     * ContractsApi.payInvoice(...) doesn't continue if Invoice is already paid.
     */
    @Test
    public void invoiceIsAlreadyPaid(){
        final User user = Mockito.mock(User.class);
        final Provider provider = Mockito.mock(Provider.class);
        final Projects projects = Mockito.mock(Projects.class);
        final Project project = this.mockActiveProject("mihai",
            "mihai", "test");
        final Contracts contracts = Mockito.mock(Contracts.class);
        final Contract.Id contractId = new Contract.Id(
            "mihai/test",
            "john",
            "github",
            "DEV"
        );
        final Contract contract = this.mockContract(
            contractId,
            project,
            BigDecimal.TEN,
            BigDecimal.TEN
        );
        final Invoice invoice = this.mockInvoice(1, true,
            null);
        final Invoice active = this.mockInvoice(2, false,
            this.mockPayment(Payment.Status.SUCCESSFUL, ""));
        final Invoices invoices = Mockito.mock(Invoices.class);

        Mockito.when(user.username()).thenReturn("mihai");
        Mockito.when(provider.name()).thenReturn("github");
        Mockito.when(user.provider()).thenReturn(provider);
        Mockito.when(user.projects()).thenReturn(projects);
        Mockito.when(projects.getProjectById("mihai/test", "github"))
            .thenReturn(project);
        Mockito.when(project.contracts()).thenReturn(contracts);
        Mockito.when(contracts.findById(contractId)).thenReturn(contract);

        Mockito.when(contract.invoices()).thenReturn(invoices);
        Mockito.when(invoices.getById(1)).thenReturn(invoice);
        Mockito.when(invoices.active()).thenReturn(active);

        final ContractsApi api = new ContractsApi(user);

        final ResponseEntity<String> resp =
            api.payInvoice("mihai", "test", "john", 1, "DEV");
        final JsonObject json = Json.createReader(
            new StringReader(Objects.requireNonNull(resp.getBody()))
        ).readObject();

        MatcherAssert.assertThat(resp.getStatusCode(),
            Matchers.equalTo(HttpStatus.OK));
        MatcherAssert.assertThat(
            json.getInt("paid"),
            Matchers.equalTo(1)
        );
        MatcherAssert.assertThat(
            json.getJsonObject("active")
                .getInt("id"),
            Matchers.equalTo(2)
        );
    }

    /**
     * Mock an activated project.
     * @param selfOwner Owner username in Self.
     * @param repoOwner Owner username from the provider
     *  (can also be an org name).
     * @param name Repo simple name.
     * @return Project.
     */
    private Project mockActiveProject(
        final String selfOwner,
        final String repoOwner,
        final String name
    ) {
        final Project project = Mockito.mock(Project.class);
        Mockito.when(project.repoFullName())
            .thenReturn(repoOwner + "/" + name);
        Mockito.when(project.provider()).thenReturn("github");

        final ProjectManager manager = Mockito.mock(ProjectManager.class);
        Mockito.when(manager.id()).thenReturn(1);
        Mockito.when(manager.userId()).thenReturn("123");
        Mockito.when(manager.username()).thenReturn("zoeself");
        Mockito.when(manager.projectPercentage()).thenReturn(6.5);
        final Provider prov = Mockito.mock(Provider.class);
        Mockito.when(prov.name()).thenReturn("github");
        Mockito.when(manager.provider()).thenReturn(prov);

        Mockito.when(project.projectManager()).thenReturn(manager);

        final User user = Mockito.mock(User.class);
        Mockito.when(user.username()).thenReturn(selfOwner);
        Mockito.when(project.owner()).thenReturn(user);

        final Wallet wallet = Mockito.mock(Wallet.class);
        Mockito.when(wallet.available()).thenReturn(BigDecimal.valueOf(1000));
        Mockito.when(wallet.cash()).thenReturn(BigDecimal.valueOf(1200));
        Mockito.when(wallet.debt()).thenReturn(BigDecimal.valueOf(200));
        Mockito.when(wallet.type()).thenReturn("fake");
        Mockito.when(project.wallet()).thenReturn(wallet);

        return project;
    }

    /**
     * Mock a Contract.
     * @param id Contract.Id.
     * @param project Project.
     * @param hourlyRate Hourly Rate.
     * @param value Value.
     * @return Contract.
     * @checkstyle ParameterNumber (10 lines).
     */
    private Contract mockContract(
        final Contract.Id id,
        final Project project,
        final BigDecimal hourlyRate,
        final BigDecimal value){
        final Contract contract = Mockito.mock(Contract.class);
        Mockito.when(contract.contractId()).thenReturn(id);
        Mockito.when(contract.project()).thenReturn(project);
        Mockito.when(contract.hourlyRate()).thenReturn(hourlyRate);
        Mockito.when(contract.value()).thenReturn(value);
        Mockito.when(contract.revenue()).thenReturn(BigDecimal.valueOf(9000));
        return contract;
    }

    /**
     * Mocks Contracts.
     * @param contract Contracts list.
     * @return Contracts.
     */
    private Contracts mockContracts(final Contract... contract){
        final Contracts contracts = Mockito.mock(Contracts.class);
        Mockito.when(contracts.iterator())
            .thenReturn(Arrays.asList(contract).iterator());
        return contracts;
    }

    /**
     * Mocks Invoice.
     * @param id Id.
     * @param isPaid Is paid.
     * @param latest Latest Payment. May be null.
     * @return Invoice.
     */
    private Invoice mockInvoice(
        final int id,
        final boolean isPaid,
        final Payment latest){
        final Invoice invoice = Mockito.mock(Invoice.class);
        Mockito.when(invoice.invoiceId()).thenReturn(id);
        Mockito.when(invoice.createdAt()).thenReturn(LocalDateTime.now());
        Mockito.when(invoice.isPaid()).thenReturn(isPaid);
        Mockito.when(invoice.amount()).thenReturn(BigDecimal.valueOf(1000));
        Mockito.when(invoice.totalAmount())
            .thenReturn(BigDecimal.valueOf(1000));
        Mockito.when(invoice.latest()).thenReturn(latest);
        return invoice;
    }

    /**
     * Mocks Payment.
     * @param status One of: {@link Payment.Status}.
     * @param failReason Fail reason.
     * @return Payment.
     */
    private Payment mockPayment(final String status, final String failReason){
        final Payment payment = Mockito.mock(Payment.class);
        Mockito.when(payment.status()).thenReturn(status);
        Mockito.when(payment.failReason()).thenReturn(failReason);
        Mockito.when(payment.transactionId()).thenReturn("tx_123");
        Mockito.when(payment.paymentTime()).thenReturn(LocalDateTime.now());
        return payment;
    }
}
