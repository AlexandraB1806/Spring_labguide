import com.luxoft.bankapp.exceptions.ActiveAccountNotSet;
import com.luxoft.bankapp.model.AbstractAccount;
import com.luxoft.bankapp.model.CheckingAccount;
import com.luxoft.bankapp.model.Client;
import com.luxoft.bankapp.model.SavingAccount;
import com.luxoft.bankapp.service.BankReportService;
import com.luxoft.bankapp.service.BankReportServiceImpl;
import com.luxoft.bankapp.service.Banking;
import com.luxoft.bankapp.service.BankingImpl;
import com.luxoft.bankapp.model.Client.Gender;
import com.luxoft.bankapp.service.storage.ClientRepository;
import com.luxoft.bankapp.service.storage.MapClientRepository;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class BankApplication {

    private static final String[] CLIENT_NAMES =
            {"Jonny Bravo", "Adam Budzinski", "Anna Smith"};

    public static void main(String[] args) {

        // Load the Spring application context
        ApplicationContext context = new ClassPathXmlApplicationContext("application-context.xml");

        // Initialize Banking bean using the modified initialize method
        Banking banking = initialize(context);

        workWithExistingClients(banking);

        bankingServiceDemo(banking);

        bankReportsDemo(context);
    }

    public static void bankReportsDemo(ApplicationContext context) {

        System.out.println("\n=== Using BankReportService ===\n");

        // Retrieve the BankReportService bean from the application context
        BankReportService bankReportService = (BankReportService) context.getBean("bankReportService");

        System.out.println("Number of clients: " + bankReportService.getNumberOfBankClients());

        System.out.println("Number of accounts: " + bankReportService.getAccountsNumber());

        System.out.println("Bank Credit Sum: " + bankReportService.getBankCreditSum());
    }

    public static void bankingServiceDemo(Banking banking) {

        System.out.println("\n=== Initialization using Banking implementation ===\n");

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

    public static void workWithExistingClients(Banking banking) {

        System.out.println("\n=======================================");
        System.out.println("\n===== Work with existing clients ======");

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
     * @param context
     * @return the Banking bean directly from the context
     */
    public static Banking initialize(ApplicationContext context) {

        // Retrieve the Banking bean from the application context
        Banking banking = (Banking) context.getBean("banking");

        Client client_1 = new Client(CLIENT_NAMES[0], Gender.MALE);

        AbstractAccount savingAccount = new SavingAccount(1000);
        client_1.addAccount(savingAccount);

        AbstractAccount checkingAccount = new CheckingAccount(1000);
        client_1.addAccount(checkingAccount);

        Client client_2 = new Client(CLIENT_NAMES[1], Gender.MALE);

        AbstractAccount checking = new CheckingAccount(1500);
        client_2.addAccount(checking);

        banking.addClient(client_1);
        banking.addClient(client_2);

        return banking;
    }
}
