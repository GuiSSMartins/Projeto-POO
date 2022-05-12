package Entities;

public class Formula1 implements FormulaConsumo{



    public double calculaTotal(double taxes, double consumption, double dailyCost, double nDevices) {
        return (nDevices > 10? dailyCost*consumption*(1+taxes)*0.9 : dailyCost*consumption*(1+taxes)*0.75);
    }


    public FormulaConsumo clone() {
        return new Formula1();
    }
}