package Entities;

public class Formula4 implements FormulaConsumo{
    public Formula4() {
    }

    public double calculaTotal(double taxes, double consumption, double dailyCost, double nDevices) {
        return nDevices*0.1+dailyCost*consumption*(1+taxes);
    }

}
