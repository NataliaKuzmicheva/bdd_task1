package ru.netology.web.test;

import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.netology.web.data.DataHelper;
import ru.netology.web.page.DashboardPage;
import ru.netology.web.page.LoginPage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.netology.web.data.DataHelper.*;

public class MoneyTransferTest {
    DashboardPage dashboardPage;
    CardInfo firstCardInfo;
    CardInfo secondCardInfo;
    int firstCardBalance;
    int secondCardBalance;

    @BeforeEach
    void setUp() {
        var info = getAuthInfo();
        var verificationCode = DataHelper.getVerificationCodeFor(info);
        var loginPage = Selenide.open("http://localhost:9999", LoginPage.class);
        var verificationPage = loginPage.validLogin(info);
        dashboardPage = verificationPage.validVerify(verificationCode);
        firstCardInfo = getFirstCardInfo();
        secondCardInfo = getSecondCardInfo();
        firstCardBalance = dashboardPage.getCardBalance(firstCardInfo);
        secondCardBalance = dashboardPage.getCardBalance(secondCardInfo);
    }

    @Test
    void shouldTransferFromFirstToSecond() {
        var amount = generateValidAmount(firstCardBalance);
        var expectedBalanceFirstCard = firstCardBalance - amount;
        var expectedBalanceSecondCard = secondCardBalance + amount;
        var transferPage = dashboardPage.selectCard(secondCardInfo);
        dashboardPage = transferPage.makeValidTransfer(String.valueOf(amount), firstCardInfo);
        var actualBalanceFirstCard = dashboardPage.getCardBalance(firstCardInfo);
        var actualBalanceSecondCard = dashboardPage.getCardBalance(secondCardInfo);
        assertEquals(expectedBalanceFirstCard, actualBalanceFirstCard);
        assertEquals(expectedBalanceSecondCard, actualBalanceSecondCard);
    }

    @Test
    void shouldTransferFromSecondToFirst() {
        var amount = generateValidAmount(secondCardBalance);
        var expectedBalanceFirstCard = secondCardBalance - amount;
        var expectedBalanceSecondCard = firstCardBalance + amount;
        var transferPage = dashboardPage.selectCard(firstCardInfo);
        dashboardPage = transferPage.makeValidTransfer(String.valueOf(amount), secondCardInfo);
        var actualBalanceFirstCard = dashboardPage.getCardBalance(secondCardInfo);
        var actualBalanceSecondCard = dashboardPage.getCardBalance(firstCardInfo);
        assertEquals(expectedBalanceFirstCard, actualBalanceFirstCard);
        assertEquals(expectedBalanceSecondCard, actualBalanceSecondCard);
    }

//    @Test
//    void shouldNotTransferUnderBalanceFromFirstToSecond() {
//        var amount = generateInvalidAmount(firstCardBalance);
//        var expectedBalanceFirstCard = firstCardBalance - amount;
//        var expectedBalanceSecondCard = secondCardBalance + amount;
//        var transferPage = dashboardPage.selectCard(secondCardInfo);
//        dashboardPage = transferPage.makeValidTransfer(String.valueOf(amount), firstCardInfo);
//        var actualBalanceFirstCard = dashboardPage.getCardBalance(firstCardInfo);
//        var actualBalanceSecondCard = dashboardPage.getCardBalance(secondCardInfo);
//        assertEquals(expectedBalanceFirstCard, actualBalanceFirstCard);
//        assertEquals(expectedBalanceSecondCard, actualBalanceSecondCard);
//    }

    @Test
    void shouldGetErrorMessageIfAmountMoreBalance() {
        var amount = generateInvalidAmount(secondCardBalance);
        var transferPage = dashboardPage.selectCard(firstCardInfo);
        transferPage.makeTransfer(String.valueOf(amount), secondCardInfo);
        transferPage.findErrorMessage("Выполнена попытка ввода суммы, превышающей остаток на карте списания");
        var actualBalanceFirstCard = dashboardPage.getCardBalance(firstCardInfo);
        var actualBalanceSecondCard = dashboardPage.getCardBalance(secondCardInfo);
        assertEquals(firstCardBalance, actualBalanceFirstCard);
        assertEquals(secondCardBalance, actualBalanceSecondCard);
    }
}
