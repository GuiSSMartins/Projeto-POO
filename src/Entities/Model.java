package Entities;

import Entities.Exceptions.*;
import Window.Window;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class Model implements Serializable {

    private Map<String, Casa> casas;
    private Map<String, Fornecedor> fornecedores;
    private Map<String, Invoice> invoices;
    private Map<String, FormulaConsumo> formulas;
    private List<Command> commands;
    private LocalDate fromDate;


    //Construtor vazio
    public Model (){

        this.casas = new HashMap<>();
        this.fornecedores = new HashMap<>();
        this.invoices = new HashMap<>();
        this.fromDate = LocalDate.now();
        this.formulas = new HashMap<>();
        this.commands = new ArrayList<>();

    }

    //Construtor
    public Model(Map<String, Casa> casas, Map<String, Fornecedor> fornecedores, Map<String, FormulaConsumo> formulas, Map<String, Invoice> invoices, LocalDate fromDate){
        this.casas = new HashMap<>();
        for(Casa casa : casas.values()){
            this.casas.put(casa.getNIF(), casa.clone());
        }

        this.fornecedores = new HashMap<>();
        for(Fornecedor f : fornecedores.values()){
            this.fornecedores.put(f.getSupplier(), f.clone());
        }

        this.invoices = new HashMap<>();
        for(Invoice in : invoices.values()){
            this.invoices.put(in.getId(),in.clone());
        }

        this.formulas = new HashMap<>();
        for(Map.Entry<String, FormulaConsumo> fc : formulas.entrySet()){
            this.formulas.put(fc.getKey(),fc.getValue().clone());
        }

        this.commands = new ArrayList<>();
        this.fromDate = fromDate;
    }

    //Atualizar data inicial da fatura
    public void setFromDate(LocalDate newDate){
        this.fromDate = newDate;
    }

    public LocalDate getFromDate(){
        return this.fromDate;
    }


    //Retornar custo total de instalação de um fornecedor
    public double getInstallationCost(String supplier) throws SupplierDoesntExists {
        if(!this.fornecedores.containsKey(supplier)){
            throw new SupplierDoesntExists("Este fornecedor: " + supplier + " não existe.");
        }
        return this.fornecedores.get(supplier).getInstallationCost();
    }

    //Adiciona fatura
    public void addInvoice(Invoice invoice){
        this.invoices.put(invoice.getId(),invoice.clone());
    }

    //Adiciona formula
    public void addFormula(String name, FormulaConsumo formula){
        this.formulas.put(name,formula);
    }

    //Retorna formula
    public FormulaConsumo getFormula(String formula) throws FormulaDoesntExist {
        if(!this.formulas.containsKey(formula)){
            throw new FormulaDoesntExist("Esta fórmula não existe.");
        }
        return this.formulas.get(formula);
    }

    //Avançar com a data gera faturas
    public void generateInvoices(LocalDate toDate){

        //Percorre todas as casas
        for(Casa casa : this.casas.values()){
            double consumption = casa.totalConsumption(this.fromDate, toDate); /* Calcula consumo total da casa */

            Fornecedor fornecedor = this.fornecedores.get(casa.getSupplier()); /* Fornecedor da casa */

            /* Calcula custo total da fatura */
            double totalCost = fornecedor.invoiceAmount(consumption,casa.devicesON(),casa.getTotalInstallationCost());

            /* Id da fatura é Data+NIF */
            String id = this.fromDate + casa.getNIF();

            /* Guarda fatura no programa */
            addInvoice(new Invoice(id, casa.getOwner(), casa.getNIF(), fornecedor.getSupplier(), this.fromDate, toDate, totalCost, consumption));

            /* Adiciona id da fatura ao fornecedor */

            fornecedor.addInvoice(id);

            /* Atualiza custo de instalação da casa para 0*/
            casa.setTotalInstallationCost(0);

        }
        /* Atualiza nova data de início da próxima fatura */
        setFromDate(toDate);
    }

    //Imprime faturas -> apenas para teste interno. Isto não vai para o trabalho
    public void printInvoices(){
        System.out.println("---| Faturas |---\n");
        for(Invoice in : this.invoices.values()){
            System.out.println(in.toString());
        }
        System.out.println("");
    }

    //Adiciona pedido na lista de espera
    public void addComandBasic(Command command){
        this.commands.add(command.clone());
    }

    //String to mode
    public int whichMode(String mode){
        int m;
        if(mode.equals("ON")){
            m = 1;
        }
        else m = 0;

        return m;
    }

    //String to Formula
    public FormulaConsumo whichFormula(String formula){
        FormulaConsumo formulaConsumo = null;
        switch (formula) {
            case "Formula1":
                formulaConsumo = new Formula1();
                break;
            case "Formula2": 
                formulaConsumo = new Formula2();
                break;
            case "Formula3": 
                formulaConsumo = new Formula3();
                break;
            case "Formula4": 
                formulaConsumo = new Formula4();
                break;
            case "Formula5": 
                formulaConsumo = new Formula5();
                break;
            case "Formula6": 
                formulaConsumo = new Formula6();
                break;
        }
        return formulaConsumo;
    }

    public void setModeAdvanced(String nif, String sd, int mode, LocalDate date) throws InvalidDateException, DateAlreadyExistsException{
        if(this.fromDate.compareTo(date) > 0){
            throw new InvalidDateException("Data inválida " +  date + ". Deve ser a partir de " + this.fromDate);
        }
        this.casas.get(nif).setMode(sd,mode,date);
    }

    //Executar 1 comando
    public void runCommand(Command command) throws DateAlreadyExistsException, InvalidDateException {
        //Se for esta data, são comandos da funcionalidade básica. Portanto tem que atualizar para data de início da próxima fatura
        //Se for funcionalidade avançada, n entra nesta condição e usa as datas passadas no ficheiro
        if(command.getDate().equals(LocalDate.parse("1998-01-27"))){
            command.setDate(this.fromDate);
        }


        switch(command.getName()){
            case "setMode": // setMode casa dispositivo mode
                setModeAdvanced(command.getCommand1(), command.getCommand2(), whichMode(command.getCommand3()), command.getDate());
                break;
            case "changeSupplier": //changeSupplier casa fornecedor
                Casa c = this.casas.get(command.getCommand1());
                c.setSupplier(command.getCommand2());
                break;
            case "changeFormula": //changeFormula supplier formula
                Fornecedor fornecedor = this.fornecedores.get(command.getCommand1());
                fornecedor.setFormulaConsumo(whichFormula(command.getCommand2()));
                break;
        }
    }

    //percorrer commands e executá-los
    public void runCommands() throws DateAlreadyExistsException, InvalidDateException{
        for(Command command : this.commands){
            runCommand(command);
        }
    }


    //avançar data
    public void moveForward(LocalDate toDate) throws DateAlreadyExistsException, InvalidDateException{
        if(this.fromDate.compareTo(toDate) >= 0){
            //se data de inicio for igual ou maior que data de fim
            throw new InvalidDateException("Data inválida " +  toDate + ". Deve ser a partir de " + this.fromDate);
        }
        generateInvoices(toDate);
        runCommands();
    }

    // Imprime até 5 NIF's por linha no terminal
    public List<String> printNIFs(){
        int linha = 0;
        List<String> strings = new ArrayList<>();
        for(Casa casa : this.casas.values()){
            strings.add(casa.getNIF() + "  ");
            linha++;
            if(linha == 5){
                strings.add("\n");
                linha = 0;
            }
        }
        return strings;
    }


    /* Queries estatísticas */

    //qual é a casa que mais gastou naquele período
    public String higherTotalCost() throws NoInvoicesAvailable{
        Casa result = null;
        double cost = Integer.MIN_VALUE;

        if(this.invoices.size() == 0){
            //Não tem faturas
            throw new NoInvoicesAvailable("Não há faturas guardadas.");
        }

        for(Invoice invoice : this.invoices.values()){
            if(invoice.getTotalCost() > cost){
                result = this.casas.get(invoice.getNif()).clone();
                cost = invoice.getTotalCost();
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Owner: ").append(result.getOwner())
                .append(" ")
                .append("NIF: ").append(result.getNIF())
                .append(" ")
                .append("Fornecedor: ").append(result.getSupplier())
                .append(" ")
                .append("Custo total: ").append(String.format("%,.2f",cost)).append("\n");



        return sb.toString();

    }

    //qual o comercializador com maior volume de facturação
    public String hasMoreInvoices() throws NoInvoicesAvailable{
        Fornecedor result = null;
        int n = Integer.MIN_VALUE;

        if(this.invoices.size() == 0){
            //Não tem faturas
            throw new NoInvoicesAvailable("Não há faturas guardadas.");
        }

        for(Fornecedor fornecedor : this.fornecedores.values()){
            if(fornecedor.countInvoices() > n){
                result = fornecedor.clone();
                n = fornecedor.countInvoices();
            }
        }

        return result.toString().concat("Quantidade: " + n);
    }

    //listar as facturas emitidas por um comercializador
    public List<Invoice> invoicesPerSupplier(String supplier){
        List<Invoice> result = new ArrayList<>();

        for(String invoice : this.fornecedores.get(supplier).getInvoices()){
            result.add(this.invoices.get(invoice).clone());
        }

        return result;
    }

    //dar uma ordenação dos maiores consumidores de energia durante um período a determinar
    public List<String> biggestEnergyConsumers(LocalDate fromDate, LocalDate toDate){
        List<Invoice> invoices = new ArrayList<>();

        for(Invoice in : this.invoices.values()){
            if(in.getFromDate().compareTo(fromDate) <= 0 && in.getToDate().compareTo(toDate) <=0){
                invoices.add(in.clone());
            }
        }

        Collections.sort(invoices);
        List<String> result = new ArrayList<>();
        int count = 1;
        for(Invoice in : invoices){
            result.add(count + "º   " + "Dono: " + in.getOwner() + " NIF: " + in.getNif() + " Total: " + in.getTotalConsumption());
            count++;
        }

        return result;
    }



    /* Opção de Inserir -> Podem ser feitas assim que são pedidas pelo utilizador */

    /* Casa */
    //Adicionar casa
    public void addCasa(Casa casa) throws HouseAlreadyExists {

        if(this.casas.containsKey(casa.getNIF())){
            throw new HouseAlreadyExists("Esta casa já existe.");
        }
        this.casas.put(casa.getNIF(),casa.clone());
    }


    /* Divisão */
    public void addLocationToHouse(String location, String nif) throws LocationAlreadyExists{
        this.casas.get(nif).addLocation(location);
    }

    /* SmartDevice */
    /* Quando criar o SmartDevice tem que passar a data do fromDate */
    public void addDeviceToHouse(String nif, String location, SmartDevice sd) throws SupplierDoesntExists, LocationDoesntExist, DateAlreadyExistsException {
        double installationCost = getInstallationCost(this.casas.get(nif).getSupplier());
        this.casas.get(nif).addDeviceToLocation(location,sd,installationCost);
    }

    /* Fornecedor */
    //Adicionar fornecedor
    public void addFornecedor(Fornecedor fornecedor) throws SupplierAlreadyExists {
        if(this.fornecedores.containsKey(fornecedor.getSupplier())){
            throw new SupplierAlreadyExists("Este fornecedor: " + fornecedor.getSupplier() + " já existe.");
        }
        this.fornecedores.put(fornecedor.getSupplier(), fornecedor.clone());
    }



    //Salvar em ficheiro objeto
    public void saveObject(String fileName) throws FileNotFoundException, IOException {
        FileOutputStream fos = new FileOutputStream(fileName);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(this);
        oos.flush();
        oos.close();
    }

 /* PARA GUI !!!!!!!!!!!!
 func pra ler o ficheiro objeto :)
 public static Model loadObject(String fileName) throws FileNotFoundException, IOException, ClassNotFoundException{
        FileInputStream fis = new FileInputStream(fileName);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Model m = (Model) ois.readObject();
        ois.close();
        return m;
    }
  */















    /* Modo avançado */
    /*
    * Enquanto há linhas no ficheiro lê
    * Se for setMode -> executa
    * Se for changeSupplier ou changeFormula -> cria command e adiciona à lista de commands
    * Se for generateInvoice -> chama função moveForward
    *
    * Modelo ficheiro:
    * ----------------
    * data pedido comandos
    * 2022-05-13 setMode casa1 d1 ON
    * 2022-05-13 setMode casa2 d2 OFF
    * 2022-05-14 changeSupplier EDP Galp
    * 2022-05-14 changeFormula EDP Formula1
    * 2022-05-15 generateInvoice  (este gera fatura com fromDate do model e toDate = 2022-05-15)
    *
    *
    * */

    /* Modo básico */
    /*
    Comando é criado com data model.getFromDate pq os comandos de setMode precisam desta data para os logs
    No modo avançado, n é criado comando para setMode, é feito logo em seguida.

     */
}
