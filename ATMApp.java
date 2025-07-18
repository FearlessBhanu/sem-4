import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

interface Transactable {
    void deposit(double amount);
    void withdraw(double amount) throws InsufficientFundsException;
    void transfer(Account toAccount, double amount) throws InsufficientFundsException;
}

abstract class Account implements Transactable {
    protected String accountNumber;
    protected String accountHolder;
    protected double balance;
    protected ArrayList<String> transactionHistory;

    public Account(String accountNumber, String accountHolder, double balance) {
        this.accountNumber = accountNumber;
        this.accountHolder = accountHolder;
        this.balance = balance;
        this.transactionHistory = new ArrayList<>();
    }

    public double getBalance() {
        return balance;
    }

    public void deposit(double amount) {
        balance += amount;
        transactionHistory.add("Deposited: $" + amount);
        System.out.println("Deposit successful! New balance: $" + balance);
    }

    public void transfer(Account toAccount, double amount) throws InsufficientFundsException {
        if (amount > balance) {
            throw new InsufficientFundsException("Transfer failed: Insufficient funds.");
        }
        this.withdraw(amount);
        toAccount.deposit(amount);
        transactionHistory.add("Transferred: $" + amount + " to " + toAccount.accountNumber);
        System.out.println("Transfer successful!");
    }

    public void printTransactionHistory() {
        System.out.println("\nTransaction History for " + accountHolder + " (" + accountNumber + "):");
        for (String transaction : transactionHistory) {
            System.out.println(transaction);
        }
    }

    public abstract void withdraw(double amount) throws InsufficientFundsException;
}

class CheckingAccount extends Account {
    private static final double OVERDRAFT_LIMIT = 100.00;

    public CheckingAccount(String accountNumber, String accountHolder, double balance) {
        super(accountNumber, accountHolder, balance);
    }

    @Override
    public void withdraw(double amount) throws InsufficientFundsException {
        if (balance + OVERDRAFT_LIMIT < amount) {
            throw new InsufficientFundsException("Withdrawal failed: Overdraft limit exceeded.");
        }
        balance -= amount;
        transactionHistory.add("Withdrawn: $" + amount);
        System.out.println("Withdrawal successful! New balance: $" + balance);
    }
}

class SavingsAccount extends Account {
    private static final double WITHDRAWAL_LIMIT = 500.00;

    public SavingsAccount(String accountNumber, String accountHolder, double balance) {
        super(accountNumber, accountHolder, balance);
    }

    @Override
    public void withdraw(double amount) throws InsufficientFundsException {
        if (amount > WITHDRAWAL_LIMIT) {
            throw new InsufficientFundsException("Withdrawal failed: Exceeds savings withdrawal limit.");
        }
        if (balance < amount) {
            throw new InsufficientFundsException("Withdrawal failed: Insufficient funds.");
        }
        balance -= amount;
        transactionHistory.add("Withdrawn: $" + amount);
        System.out.println("Withdrawal successful! New balance: $" + balance);
    }
}

class InsufficientFundsException extends Exception {
    public InsufficientFundsException(String message) {
        super(message);
    }
}

class User {
    private String name;
    private String pin;
    private HashMap<String, Account> accounts;

    public User(String name, String pin) {
        this.name = name;
        this.pin = pin;
        this.accounts = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public void addAccount(Account account) {
        accounts.put(account.accountNumber, account);
    }

    public Account getAccount(String accountNumber) {
        return accounts.get(accountNumber);
    }

    public boolean authenticate(String enteredPin) {
        return this.pin.equals(enteredPin);
    }

    public void printAccounts() {
        System.out.println("Accounts for " + name + ":");
        for (String accNum : accounts.keySet()) {
            System.out.println(" - " + accNum);
        }
    }
}

class ATM {
    private HashMap<String, User> users;
    private Scanner scanner;

    public ATM(Scanner scanner) {
        this.users = new HashMap<>();
        this.scanner = scanner;
    }

    public void registerUser(User user) {
        users.put(user.getName(), user);
    }

    public void start() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        User user = users.get(username);
        if (user == null) {
            System.out.println("User not found!");
            return;
        }

        System.out.print("Enter PIN: ");
        String enteredPin = scanner.nextLine();

        if (!user.authenticate(enteredPin)) {
            System.out.println("Invalid PIN!");
            return;
        }

        System.out.println("Login successful!");
        user.printAccounts();

        System.out.print("Enter account number: ");
        String accountNumber = scanner.nextLine();

        Account account = user.getAccount(accountNumber);
        if (account == null) {
            System.out.println("Invalid account number!");
            return;
        }

        while (true) {
            System.out.println("\n1. Check Balance\n2. Deposit\n3. Withdraw\n4. Transfer\n5. Transaction History\n6. Exit");
            System.out.print("Choose an option: ");

            int choice;
            try {
                choice = scanner.nextInt();
            } catch (Exception e) {
                System.out.println("Invalid input! Please enter a number.");
                scanner.nextLine(); // clear invalid input
                continue;
            }

            try {
                switch (choice) {
                    case 1:
                        System.out.println("Balance: $" + account.getBalance());
                        break;
                    case 2:
                        System.out.print("Enter deposit amount: ");
                        account.deposit(scanner.nextDouble());
                        break;
                    case 3:
                        System.out.print("Enter withdrawal amount: ");
                        account.withdraw(scanner.nextDouble());
                        break;
                    case 4:
                        scanner.nextLine(); // consume leftover newline
                        System.out.print("Enter target account number: ");
                        String targetAccountNumber = scanner.nextLine();
                        Account targetAccount = user.getAccount(targetAccountNumber);
                        if (targetAccount == null) {
                            System.out.println("Invalid target account!");
                            break;
                        }
                        System.out.print("Enter transfer amount: ");
                        account.transfer(targetAccount, scanner.nextDouble());
                        break;
                    case 5:
                        account.printTransactionHistory();
                        break;
                    case 6:
                        System.out.println("Goodbye!");
                        return;
                    default:
                        System.out.println("Invalid choice!");
                }
            } catch (InsufficientFundsException e) {
                System.out.println(e.getMessage());
            } catch (Exception e) {
                System.out.println("Something went wrong. Please try again.");
                scanner.nextLine(); // to clear the buffer
            }
        }
    }
}

public class ATMApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ATM atm = new ATM(scanner);

        User user1 = new User("Alice", "1234");
        user1.addAccount(new CheckingAccount("CHK123", "Alice", 1000));
        user1.addAccount(new SavingsAccount("SAV123", "Alice", 5000));

        atm.registerUser(user1);
        atm.start();

        scanner.close(); // important to close the scanner at the end
    }
}