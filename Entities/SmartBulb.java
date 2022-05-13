package Entities;

import Enums.Mode;
import Enums.Tone;

import java.time.LocalDate;

public class SmartBulb extends SmartDevice{
    private Tone tone;
    private double dimension;

    //Construtor vazio
    public SmartBulb(){
        super();
        this.tone = Tone.NEUTRAL;
        this.dimension = 0.0;
    }

    //Construtor completo
    public SmartBulb(String id, Tone tone, double consumptionBase, double dimension, Mode mode, LocalDate fromDate) {
        super(id, mode,consumptionBase, fromDate);
        this.tone = tone;
        this.dimension = dimension;
    }

    //Construtor sem Mode e Tone
    public SmartBulb(String id, double consumptionBase, double dimension, LocalDate fromDate) {
        super(id, consumptionBase, fromDate);
        this.tone = Tone.COLD;
        this.dimension = dimension;
    }

    //Construtor cópia
    public SmartBulb(SmartBulb sb) {
        // initialise instance variables
        super(sb);
        this.tone = sb.getTone();
        this.dimension = sb.getDimension();
    }

    //Getters e Setters
    public Tone getTone() {
        return tone;
    }

    public void setTone(Tone t) {
        this.tone = t;
    }

    public double getDimension() {
        return dimension;
    }

    public void setDimension(double dimension) {
        this.dimension = dimension;
    }

    //Equals
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || o.getClass() != this.getClass()) return false;
        SmartBulb sb = (SmartBulb) o;
        return (super.equals(sb) &&
                this.tone == sb.getTone() &&
                this.dimension == sb.getDimension());
    }

    //Clone
    public SmartBulb clone() {
        return new SmartBulb(this);
    }

    //Calcula consumo energético da lâmpada
    public double consumoEnergetico(){
        double valor;
        switch(this.tone){
            case COLD:
                valor = super.getConsumptionBase()*0.15;
                break;
            case WARM:
                valor = super.getConsumptionBase()*0.35;
                break;
            default:
                valor = super.getConsumptionBase()*0.22;
                break;
        }
        return super.getConsumptionBase() + valor;
    }


}
