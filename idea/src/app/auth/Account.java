package app.auth;

import app.util.Utils;
import app.util.data.DataSerializer;
import app.util.data.DataSerializers;

import java.util.UUID;

public class Account {
    private final AccountType accountType;
    private final UUID uuid;
    private String email;
    private String displayName;
    private String passwordHash;

    public Account(AccountType accountType, UUID uuid, String email, String displayName, String passwordHash) {
        this.accountType = accountType;
        this.uuid = uuid;
        this.email = email;
        this.displayName = displayName;
        this.passwordHash = passwordHash;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public static class Serializer extends DataSerializer<Account> {
        public Serializer() {
            super(Account.class);
        }

        @Override
        public String serialize(Account value) {
            return DataSerializers.writeSegmentedLine(Utils.allToStrings(value.getAccountType().name(), value.getUUID(), value.getEmail(), value.getDisplayName(), value.getPasswordHash()));
        }

        @Override
        public Account deserialize(String data) {
            var split = DataSerializers.readSegmentedLine(data);
            return new Account(AccountType.valueOf(split.get(0)), UUID.fromString(split.get(1)), split.get(2), split.get(3), split.get(4));
        }
    }

    static {
        DataSerializers.register("account", new Account.Serializer());
    }

    public static void init() {}
}
