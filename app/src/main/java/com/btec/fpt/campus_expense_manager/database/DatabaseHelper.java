package com.btec.fpt.campus_expense_manager.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;

import com.btec.fpt.campus_expense_manager.entities.Transaction;
import com.btec.fpt.campus_expense_manager.entities.User;
import com.btec.fpt.campus_expense_manager.entities.Category;
import com.btec.fpt.campus_expense_manager.models.BalanceInfor;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ExpenseDB";
    private static final int DATABASE_VERSION = 6;

    // Transactions table
    private static final String TABLE_TRANSACTION = "transactions";
    private static final String COLUMN_TRANSACTION_ID = "id";
    private static final String COLUMN_AMOUNT = "amount";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_CATEGORY = "category";

    // User table
    private static final String TABLE_USER = "USER";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_FIRST_NAME = "firstName";
    private static final String COLUMN_LAST_NAME = "lastName";
    private static final String COLUMN_PASSWORD = "password";

    // Category table
    private static final String TABLE_CATEGORY = "CATEGORY";
    private static final String COLUMN_CATEGORY_ID = "category_id";
    private static final String COLUMN_CATEGORY_NAME = "name";

    // Budgets table
    private static final String TABLE_BUDGET = "budgets";
    private static final String COLUMN_BUDGET_ID = "budget_id";
    private static final String COLUMN_BUDGET_AMOUNT = "budget_amount";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create transactions table
        String CREATE_TRANSACTION_TABLE = "CREATE TABLE " + TABLE_TRANSACTION + "("
                + COLUMN_TRANSACTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_AMOUNT + " REAL, "
                + COLUMN_DESCRIPTION + " TEXT, "
                + COLUMN_DATE + " TEXT,"
                + COLUMN_TYPE + " INTEGER,"
                + COLUMN_EMAIL + " TEXT,"
                + COLUMN_CATEGORY + " TEXT" +
                ")";
        db.execSQL(CREATE_TRANSACTION_TABLE);

        // Create user table
        String CREATE_USER_TABLE = "CREATE TABLE " + TABLE_USER + " ("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_FIRST_NAME + " TEXT, "
                + COLUMN_LAST_NAME + " TEXT, "
                + COLUMN_EMAIL + " TEXT UNIQUE, "
                + COLUMN_PASSWORD + " TEXT" + ")";
        db.execSQL(CREATE_USER_TABLE);

        // Create category table
        String CREATE_CATEGORY_TABLE = "CREATE TABLE " + TABLE_CATEGORY + "("
                + COLUMN_CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_CATEGORY_NAME + " TEXT NOT NULL,"
                + COLUMN_EMAIL + " TEXT" +
                ")";
        db.execSQL(CREATE_CATEGORY_TABLE);

        // Create budgets table
        String CREATE_BUDGET_TABLE = "CREATE TABLE " + TABLE_BUDGET + "("
                + COLUMN_BUDGET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_EMAIL + " TEXT, "
                + COLUMN_CATEGORY + " TEXT, "
                + COLUMN_BUDGET_AMOUNT + " REAL" +
                ")";
        db.execSQL(CREATE_BUDGET_TABLE);

        insertDefaultCategories(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDGET);
        onCreate(db);
    }

    // Budget-related methods
    public boolean insertBudget(String email, String category, double budgetAmount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_BUDGET_AMOUNT, budgetAmount);

        long result = db.insert(TABLE_BUDGET, null, values);
        db.close();
        return result != -1;
    }

    public double getBudgetForCategory(String email, String category) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_BUDGET_AMOUNT + " FROM " + TABLE_BUDGET +
                " WHERE " + COLUMN_EMAIL + " = ? AND " + COLUMN_CATEGORY + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email, category});

        double budget = 0;
        if (cursor.moveToFirst()) {
            budget = cursor.getDouble(0);
        }
        cursor.close();
        db.close();
        return budget;
    }

    public double getTotalExpensesForCategory(String email, String category) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(" + COLUMN_AMOUNT + ") FROM " + TABLE_TRANSACTION +
                " WHERE " + COLUMN_EMAIL + " = ? AND " + COLUMN_CATEGORY + " = ? AND " + COLUMN_TYPE + " = 0";
        Cursor cursor = db.rawQuery(query, new String[]{email, category});

        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        db.close();
        return total;
    }

    public double getRemainingBudgetForCategory(String email, String category) {
        double budget = getBudgetForCategory(email, category);
        double totalExpenses = getTotalExpensesForCategory(email, category);
        return budget - totalExpenses;
    }

    public void insertSampleBudgets(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Xóa ngân sách cũ (nếu cần)
        db.execSQL("DELETE FROM " + TABLE_BUDGET + " WHERE " + COLUMN_EMAIL + " = ?", new String[]{email});

        // Thêm ngân sách mẫu
        insertBudget(email, "Food", 1000000); // 1,000,000 VND
        insertBudget(email, "Transport", 500000); // 500,000 VND
        insertBudget(email, "Entertainment", 300000); // 300,000 VND
        db.close();
    }

    public boolean insertCategory(String email, String categoryName) {
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "SELECT * FROM " + TABLE_CATEGORY +
                " WHERE LOWER(" + COLUMN_CATEGORY_NAME + ") = LOWER(?) AND (" + COLUMN_EMAIL + " = ? OR " + COLUMN_EMAIL + " IS NULL)";
        Cursor cursor = db.rawQuery(query, new String[]{categoryName, email});

        if (cursor.getCount() > 0) {
            cursor.close();
            db.close();
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_CATEGORY_NAME, categoryName);
        values.put(COLUMN_EMAIL, email);

        long result = db.insert(TABLE_CATEGORY, null, values);
        cursor.close();
        db.close();
        return result != -1;
    }

    public boolean updateCategory(int categoryId, String name, String email) {
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "SELECT * FROM " + TABLE_CATEGORY +
                " WHERE LOWER(" + COLUMN_CATEGORY_NAME + ") = LOWER(?) AND " + COLUMN_EMAIL + " = ? AND " + COLUMN_CATEGORY_ID + " != ?";
        Cursor cursor = db.rawQuery(query, new String[]{name, email, String.valueOf(categoryId)});

        if (cursor.getCount() > 0) {
            cursor.close();
            db.close();
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_CATEGORY_NAME, name);
        values.put(COLUMN_EMAIL, email);

        int rowsAffected = db.update(TABLE_CATEGORY, values, COLUMN_CATEGORY_ID + " = ?", new String[]{String.valueOf(categoryId)});
        cursor.close();
        db.close();
        return rowsAffected > 0;
    }

    public boolean deleteCategory(int categoryId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_CATEGORY, COLUMN_CATEGORY_ID + " = ?", new String[]{String.valueOf(categoryId)});
        db.close();
        return rowsDeleted > 0;
    }

    public List<Category> getAllCategoryByEmail(String email) {
        List<Category> categoryList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_CATEGORY + " WHERE " + COLUMN_EMAIL + " = ? OR " + COLUMN_EMAIL + " IS NULL";
        Cursor cursor = db.rawQuery(query, new String[]{email});

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(COLUMN_CATEGORY_ID));
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY_NAME));
                @SuppressLint("Range") String categoryEmail = cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL));

                Category category = new Category(id, name, categoryEmail);
                categoryList.add(category);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return categoryList;
    }

    private void insertDefaultCategories(SQLiteDatabase db) {
        String[] defaultCategories = {"Food", "Transport", "Entertainment", "Utilities", "Health", "House Fee", "Shopping", "Salary"};
        for (String categoryName : defaultCategories) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_CATEGORY_NAME, categoryName);
            values.put(COLUMN_EMAIL, (String) null);
            db.insert(TABLE_CATEGORY, null, values);
        }
    }

    public boolean insertTransaction(double amount, String description, String date, int type, String email, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_TYPE, type);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_CATEGORY, category);

        long result = db.insert(TABLE_TRANSACTION, null, values);
        db.close();
        return result != -1;
    }

    public void insertSampleData(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Xóa dữ liệu cũ (nếu cần)
        db.execSQL("DELETE FROM " + TABLE_TRANSACTION + " WHERE " + COLUMN_EMAIL + " = ?", new String[]{email});

        // Thêm dữ liệu mẫu
        ContentValues values1 = new ContentValues();
        values1.put(COLUMN_AMOUNT, 500000);
        values1.put(COLUMN_DESCRIPTION, "Lunch");
        values1.put(COLUMN_DATE, "2025-04-15");
        values1.put(COLUMN_TYPE, 0); // Expense
        values1.put(COLUMN_EMAIL, email);
        values1.put(COLUMN_CATEGORY, "Food");
        db.insert(TABLE_TRANSACTION, null, values1);

        ContentValues values2 = new ContentValues();
        values2.put(COLUMN_AMOUNT, 200000);
        values2.put(COLUMN_DESCRIPTION, "Bus fare");
        values2.put(COLUMN_DATE, "2025-04-15");
        values2.put(COLUMN_TYPE, 0); // Expense
        values2.put(COLUMN_EMAIL, email);
        values2.put(COLUMN_CATEGORY, "Transport");
        db.insert(TABLE_TRANSACTION, null, values2);

        ContentValues values3 = new ContentValues();
        values3.put(COLUMN_AMOUNT, 100000);
        values3.put(COLUMN_DESCRIPTION, "Movie ticket");
        values3.put(COLUMN_DATE, "2025-04-15");
        values3.put(COLUMN_TYPE, 0); // Expense
        values3.put(COLUMN_EMAIL, email);
        values3.put(COLUMN_CATEGORY, "Entertainment");
        db.insert(TABLE_TRANSACTION, null, values3);

        db.close();
    }

    public boolean updateTransaction(int id, double amount, String description, String date, int type, String email, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_TYPE, type);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_CATEGORY, category);

        int rowsAffected = db.update(TABLE_TRANSACTION, values, COLUMN_TRANSACTION_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rowsAffected > 0;
    }

    public boolean deleteTransaction(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_TRANSACTION, COLUMN_TRANSACTION_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rowsDeleted > 0;
    }

    public void clearAllTransactions() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_TRANSACTION);
        db.close();
    }

    public ArrayList<Transaction> getFilteredTransactions(String startDate, String endDate, String category) {
        ArrayList<Transaction> transactions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_TRANSACTION + " WHERE 1=1";
        ArrayList<String> args = new ArrayList<>();

        if (!startDate.isEmpty()) {
            query += " AND " + COLUMN_DATE + " >= ?";
            args.add(startDate);
        }
        if (!endDate.isEmpty()) {
            query += " AND " + COLUMN_DATE + " <= ?";
            args.add(endDate);
        }
        if (!category.isEmpty()) {
            query += " AND " + COLUMN_CATEGORY + " = ?";
            args.add(category);
        }

        Cursor cursor = db.rawQuery(query, args.toArray(new String[0]));

        if (cursor.moveToFirst()) {
            do {
                Transaction transaction = new Transaction(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_ID)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TYPE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY))
                );
                transactions.add(transaction);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return transactions;
    }

    public boolean insertUser(String firstName, String lastName, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FIRST_NAME, firstName);
        values.put(COLUMN_LAST_NAME, lastName);
        values.put(COLUMN_EMAIL, email);

        String hashPassword = hashPassword(password);
        values.put(COLUMN_PASSWORD, hashPassword);

        long result = db.insert(TABLE_USER, null, values);
        db.close();
        return result != -1;
    }

    public boolean updateUser(int userId, String firstName, String lastName, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FIRST_NAME, firstName);
        values.put(COLUMN_LAST_NAME, lastName);
        values.put(COLUMN_EMAIL, email);
        String hashPassword = hashPassword(password);
        values.put(COLUMN_PASSWORD, hashPassword);

        int rowsAffected = db.update(TABLE_USER, values, COLUMN_USER_ID + " = ?", new String[]{String.valueOf(userId)});
        db.close();
        return rowsAffected > 0;
    }

    public boolean deleteUser(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_USER, COLUMN_USER_ID + " = ?", new String[]{String.valueOf(userId)});
        db.close();
        return rowsDeleted > 0;
    }

    public List<Category> getCategoryList() {
        List<Category> categoryList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CATEGORY, null);

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(COLUMN_CATEGORY_ID));
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY_NAME));
                @SuppressLint("Range") String email = cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL));

                Category category = new Category(id, name, email);
                categoryList.add(category);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return categoryList;
    }

    public List<Transaction> getTransactionList() {
        List<Transaction> transactionList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TRANSACTION, null);

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(COLUMN_TRANSACTION_ID));
                @SuppressLint("Range") double amount = cursor.getDouble(cursor.getColumnIndex(COLUMN_AMOUNT));
                @SuppressLint("Range") String description = cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION));
                @SuppressLint("Range") String date = cursor.getString(cursor.getColumnIndex(COLUMN_DATE));
                @SuppressLint("Range") int type = cursor.getInt(cursor.getColumnIndex(COLUMN_TYPE));
                @SuppressLint("Range") String email = cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL));
                @SuppressLint("Range") String category = cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY));

                Transaction transaction = new Transaction(id, amount, description, date, type, email, category);
                transactionList.add(transaction);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return transactionList;
    }

    public List<Transaction> getAllTransactionsByEmail(String email) {
        List<Transaction> transactionList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_TRANSACTION + " WHERE " + COLUMN_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(COLUMN_TRANSACTION_ID));
                @SuppressLint("Range") double amount = cursor.getDouble(cursor.getColumnIndex(COLUMN_AMOUNT));
                @SuppressLint("Range") String description = cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION));
                @SuppressLint("Range") String date = cursor.getString(cursor.getColumnIndex(COLUMN_DATE));
                @SuppressLint("Range") int type = cursor.getInt(cursor.getColumnIndex(COLUMN_TYPE));
                @SuppressLint("Range") String email2 = cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL));
                @SuppressLint("Range") String category = cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY));

                Transaction transaction = new Transaction(id, amount, description, date, type, email2, category);
                transactionList.add(transaction);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return transactionList;
    }

    public User getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        User user = null;
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USER + " WHERE " + COLUMN_EMAIL + " = ?", new String[]{email});

        if (cursor.moveToFirst()) {
            @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(COLUMN_USER_ID));
            @SuppressLint("Range") String firstName = cursor.getString(cursor.getColumnIndex(COLUMN_FIRST_NAME));
            @SuppressLint("Range") String lastName = cursor.getString(cursor.getColumnIndex(COLUMN_LAST_NAME));
            @SuppressLint("Range") String userEmail = cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL));
            @SuppressLint("Range") String password = cursor.getString(cursor.getColumnIndex(COLUMN_PASSWORD));

            user = new User(id, firstName, lastName, userEmail, password);
        }

        cursor.close();
        db.close();
        return user;
    }

    public List<User> getUserList() {
        List<User> userList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USER, null);

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(COLUMN_USER_ID));
                @SuppressLint("Range") String firstName = cursor.getString(cursor.getColumnIndex(COLUMN_FIRST_NAME));
                @SuppressLint("Range") String lastName = cursor.getString(cursor.getColumnIndex(COLUMN_LAST_NAME));
                @SuppressLint("Range") String email = cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL));
                @SuppressLint("Range") String password = cursor.getString(cursor.getColumnIndex(COLUMN_PASSWORD));

                User user = new User(id, firstName, lastName, email, password);
                userList.add(user);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return userList;
    }

    public BalanceInfor getBalanceFromEmail(String email) {
        User userFound = getUserByEmail(email);
        if (userFound != null) {
            String firstName = userFound.getFirstName();
            String lastName = userFound.getLastName();

            List<Transaction> allTransaction = getAllTransactionsByEmail(email);

            double expense = 0;
            double income = 0;
            double balance = 0;
            for (Transaction transaction : allTransaction) {
                if (transaction.getType() == 0) {
                    expense += transaction.getAmount();
                } else if (transaction.getType() == 1) {
                    income += transaction.getAmount();
                }
            }

            balance = income - expense;

            BalanceInfor balanceInfor = new BalanceInfor();
            balanceInfor.setBalance(balance);
            balanceInfor.setFirstName(firstName);
            balanceInfor.setLastName(lastName);

            return balanceInfor;
        }

        return null;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.encodeToString(hash, Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean signUp(String firstName, String lastName, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USER + " WHERE " + COLUMN_EMAIL + " = ?", new String[]{email});

        if (cursor.getCount() > 0) {
            cursor.close();
            db.close();
            return false;
        }

        String hashedPassword = hashPassword(password);
        if (hashedPassword == null) {
            cursor.close();
            db.close();
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_FIRST_NAME, firstName);
        values.put(COLUMN_LAST_NAME, lastName);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, hashedPassword);

        long result = db.insert(TABLE_USER, null, values);
        cursor.close();
        db.close();
        return result != -1;
    }

    public boolean signIn(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();

        String hashedPassword = hashPassword(password);
        if (hashedPassword == null) {
            db.close();
            return false;
        }

        String query = "SELECT * FROM " + TABLE_USER + " WHERE " + COLUMN_EMAIL + " = ? AND " + COLUMN_PASSWORD + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email, hashedPassword});

        boolean isAuthenticated = cursor.getCount() > 0;
        cursor.close();
        db.close();

        return isAuthenticated;
    }

    public boolean changePassword(String email, String oldPassword, String newPassword) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT " + COLUMN_PASSWORD + " FROM " + TABLE_USER + " WHERE " + COLUMN_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});

        if (cursor != null && cursor.moveToFirst()) {
            String storedHashedPassword = cursor.getString(0);
            cursor.close();

            String hashedOldPassword = hashPassword(oldPassword);
            if (storedHashedPassword.equals(hashedOldPassword)) {
                db = this.getWritableDatabase();
                ContentValues values = new ContentValues();
                String hashedNewPassword = hashPassword(newPassword);
                values.put(COLUMN_PASSWORD, hashedNewPassword);

                int rowsAffected = db.update(TABLE_USER, values, COLUMN_EMAIL + " = ?", new String[]{email});
                db.close();
                return rowsAffected > 0;
            } else {
                cursor.close();
                return false;
            }
        }

        if (cursor != null) cursor.close();
        db.close();
        return false;
    }

    public HashMap<String, Double> getSpendingByCategory(String email) {
        HashMap<String, Double> spending = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT category, SUM(amount) AS total FROM transactions WHERE email = ? AND type = 0 GROUP BY category";

        Cursor cursor = db.rawQuery(query, new String[]{email});
        if (cursor.moveToFirst()) {
            do {
                String category = cursor.getString(0);
                double total = cursor.getDouble(1);
                spending.put(category, total);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return spending;
    }

    public HashMap<String, Double> getCategoryFluctuations(String email) {
        HashMap<String, Double> fluctuations = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT category, MAX(amount) - MIN(amount) AS fluctuation FROM transactions WHERE email = ? GROUP BY category";

        Cursor cursor = db.rawQuery(query, new String[]{email});
        if (cursor.moveToFirst()) {
            do {
                String category = cursor.getString(0);
                double fluctuation = cursor.getDouble(1);
                fluctuations.put(category, fluctuation);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return fluctuations;
    }
}