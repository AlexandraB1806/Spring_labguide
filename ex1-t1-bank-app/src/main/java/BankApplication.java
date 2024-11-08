import com.luxoft.bankapp.exceptions.ActiveAccountNotSet;
import com.luxoft.bankapp.model.AbstractAccount;
import com.luxoft.bankapp.model.CheckingAccount;
import com.luxoft.bankapp.model.Client;
import com.luxoft.bankapp.model.SavingAccount;
import com.luxoft.bankapp.service.BankReportService;
import com.luxoft.bankapp.service.Banking;
import com.luxoft.bankapp.service.BankingImpl;
import com.luxoft.bankapp.model.Client.Gender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;

@Configuration
@ComponentScan("com.luxoft.bankapp")
@PropertySource("classpath:clients.properties")
public class BankApplication {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private Environment environment;

    @Bean(name = "checkingAccount1")
    public CheckingAccount getDemoCheckingAccount1(@Value("${client1.checkingAccountInitialValue:0}") double overdraft) {
        return new CheckingAccount(overdraft);
    }

    @Bean(name = "savingAccount1")
    public SavingAccount getDemoSavingAccount1(@Value("${client1.savingAccountInitialValue:0}") double initialBalance) {
        return new SavingAccount(initialBalance);
    }

    @Bean(name = "client1")
    public Client getDemoClient1() {
        String name = environment.getProperty("client1.name");

        Client client = new Client(name, Gender.MALE);
        client.setCity(environment.getProperty("client1.city"));

        AbstractAccount checkingAccount = (CheckingAccount) applicationContext.getBean("checkingAccount1");
        SavingAccount savingAccount = (SavingAccount) applicationContext.getBean("savingAccount1");

        client.addAccount(checkingAccount);
        client.addAccount(savingAccount);

        return client;
    }

    @Bean(name = "checkingAccount2")
    public CheckingAccount getDemoCheckingAccount2(@Value("${client2.checkingAccountInitialValue:0}") double overdraft) {
        return new CheckingAccount(overdraft);
    }

    @Bean(name = "client2")
    public Client getDemoClient2() {
        String name = environment.getProperty("client2.name");

        Client client = new Client(name, Gender.MALE);
        client.setCity(environment.getProperty("client2.city"));

        AbstractAccount checkingAccount = (CheckingAccount) applicationContext.getBean("checkingAccount2");

        client.addAccount(checkingAccount);

        return client;
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    private static final String[] CLIENT_NAMES =
            {"Jonny Bravo", "Adam Budzinski", "Anna Smith"};

    public static void main(String[] args) {

        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(BankApplication.class);

        initialize(applicationContext);

        workWithExistingClients(applicationContext);

        bankingServiceDemo(applicationContext);

        bankReportsDemo(applicationContext);
    }

    public static void bankReportsDemo(ApplicationContext context) {

        System.out.println("\n=== Using BankReportService ===\n");

        BankReportService bankReportService = context.getBean(BankReportService.class);

        System.out.println("Number of clients: " + bankReportService.getNumberOfBankClients());

        System.out.println("Number of accounts: " + bankReportService.getAccountsNumber());

        System.out.println("Bank Credit Sum: " + bankReportService.getBankCreditSum());
    }

    public static void bankingServiceDemo(ApplicationContext context) {

        System.out.println("\n=== Initialization using Banking implementation ===\n");

        Banking banking = context.getBean(BankingImpl.class);

        Client anna = new Client(CLIENT_NAMES[2], Gender.FEMALE);
        anna = banking.addClient(anna);

        AbstractAccount saving = banking.createAccount(anna, SavingAccount.class);
        saving.deposit(1000);

        banking.updateAccount(anna, saving);

        AbstractAccount checking = banking.createAccount(anna, CheckingAccount.class);
        checking.deposit(3000);

        banking.updateAccount(anna, checking);

        banking.getAllAccounts(anna).stream().forEach(System.out::println);
    }

    public static void workWithExistingClients(ApplicationContext context) {

        System.out.println("\n=======================================");
        System.out.println("\n===== Work with existing clients ======");

        Banking banking = context.getBean(BankingImpl.class);

        Client jonny = banking.getClient(CLIENT_NAMES[0]);

        try {
            jonny.deposit(5_000);
        } catch (ActiveAccountNotSet e) {
            System.out.println(e.getMessage());

            jonny.setDefaultActiveAccountIfNotSet();
            jonny.deposit(5_000);
        }

        System.out.println(jonny);

        Client adam = banking.getClient(CLIENT_NAMES[1]);
        adam.setDefaultActiveAccountIfNotSet();

        adam.withdraw(1500);

        double balance = adam.getBalance();
        System.out.println("\n" + adam.getName() + ", current balance: " + balance);

        banking.transferMoney(jonny, adam, 1000);

        System.out.println("\n=======================================");
        banking.getClients().forEach(System.out::println);
    }

    /*
     * Method that creates a few clients and initializes them with sample values
     */

    /**
     * This method now takes ApplicationContext as a parameter. No longer need to
     * manually set up the repository in BankingImpl since Spring handles the injection.
     */
    public static void initialize(ApplicationContext context) {

        Banking banking = context.getBean(BankingImpl.class);

        Client client_1 = (Client) context.getBean("client1");
        Client client_2 = (Client) context.getBean("client2");

        banking.addClient(client_1);
        banking.addClient(client_2);
    }
}
