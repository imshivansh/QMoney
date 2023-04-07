
package com.crio.warmup.stock;


import com.crio.warmup.stock.dto.*;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.nio.file.Files;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.core.NestedRuntimeException;
import org.springframework.web.client.RestTemplate;


public class PortfolioManagerApplication {

  final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy MM dd");
  public static RestTemplate restTemplate = new RestTemplate();
  public static PortfolioManager portfolioManager = PortfolioManagerFactory.getPortfolioManager(restTemplate);

  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {
    List<String>ans = new ArrayList<>();
    ObjectMapper objectMapper =  getObjectMapper();
    
    File inputFile = resolveFileFromResources(args[0]);
    PortfolioTrade[] portfolioTrade = objectMapper.readValue(inputFile, PortfolioTrade[].class);
    for(PortfolioTrade trade: portfolioTrade){
      ans.add(trade.getSymbol());
    }
    return ans;
  }


  



  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
  }

  private static File resolveFileFromResources(String filename) throws URISyntaxException ,IOException, RuntimeException {
    return Paths.get(
        Thread.currentThread().getContextClassLoader().getResource(filename).toURI()).toFile();
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }


  

  public static List<String> debugOutputs() {

     String valueOfArgument0 = "trades.json";


     String resultOfResolveFilePathArgs0 = "/home/crio-user/workspace/shivanshs977-ME_QMONEY_V2/qmoney/bin/main/trades.json";
     String toStringOfObjectMapper = "com.fasterxml.jackson.databind.ObjectMapper@5542c4ed";
     String functionNameFromTestFileInStackTrace = "PortfolioManagerApplicationTest.mainReadFile()";
     String lineNumberFromTestFileInStackTrace = "29";
// 20fc3ace9581a5beb1594642fee6a83d67644b07  api token

    return Arrays.asList(new String[]{valueOfArgument0, resultOfResolveFilePathArgs0,
        toStringOfObjectMapper, functionNameFromTestFileInStackTrace,
        lineNumberFromTestFileInStackTrace});
  }

  public static Double getClosingPriceOnEndDate(List<Candle> candles) {
    return candles.get(candles.size() - 1).getClose();
  }
  
  
  public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token) {
    String url = prepareUrl(trade, endDate, token);
    RestTemplate restTemplate = new RestTemplate();
    Candle[] candles = restTemplate.getForObject(url, TiingoCandle[].class);
    return Arrays.asList(candles);
    
  }
  public static String getToken(){
    return "9104fffb31297d3db64a2fc69778ba110486b078";
  }
  

  // Remember to confirm that you are getting same results for annualized returns as in Module 3.
  public static List<String> mainReadQuotes(String[] args) throws Exception,NestedRuntimeException, RuntimeException,IOException, URISyntaxException {
//      String  apiToken = "20fc3ace9581a5beb1594642fee6a83d67644b07";

List<PortfolioTrade> pma = readTradesFromJson(args[0]);
List<TotalReturnsDto> list = new ArrayList<>();
for (int i = 0; i < pma.size(); i++) {
  List<Candle> candles = fetchCandles(pma.get(i), LocalDate.parse(args[1]),PortfolioManagerApplication.getToken() );
  Double closingPrice=getClosingPriceOnEndDate(candles);
  list.add(new TotalReturnsDto(pma.get(i).getSymbol(), closingPrice));
}
Collections.sort(list, new Comparator<TotalReturnsDto>() {
  @Override
  public int compare(TotalReturnsDto p1, TotalReturnsDto p2) {
    return p1.getClosingPrice() > p2.getClosingPrice() ? 1 : -1;
  }
});
List<String> ans = new ArrayList<>();
for (TotalReturnsDto t : list) {
  ans.add(t.getSymbol());
}
return ans;

  }

  // TODO:
  //  After refactor, make sure that the tests pass by using these two commands
  //  ./gradlew test --tests PortfolioManagerApplicationTest.readTradesFromJson
  //  ./gradlew test --tests PortfolioManagerApplicationTest.mainReadFile
  public static List<PortfolioTrade> readTradesFromJson(String filename) throws IOException, URISyntaxException {
    List<PortfolioTrade>ans= new ArrayList<>();
    ObjectMapper objectMapper = getObjectMapper();
    File file = resolveFileFromResources(filename);
    PortfolioTrade[] trade = objectMapper.readValue(file, PortfolioTrade[].class);
    for(PortfolioTrade trades:trade){
      ans.add(trades);
    }
     return ans;
  }


  // TODO:
  //  Build the Url using given parameters and use this function in your code to cann the API.
  public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token){

    String url = "https://api.tiingo.com/tiingo/daily/" + trade.getSymbol()
    + "/prices?startDate="
    + trade.getPurchaseDate() + "&endDate=" + endDate.toString()
    + "&token="+token;
    return url;

  }
  static Double getOpeningPriceOnStartDate(List<Candle> candles) {
    return candles.get(0).getOpen();
 }


 

 public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args)
     throws IOException, URISyntaxException {
      List<AnnualizedReturn>annualList = new ArrayList<>();
      List<PortfolioTrade> pma = readTradesFromJson(args[0]);
      for(int i =0;i<pma.size();i++){
        List<Candle> candles = fetchCandles(pma.get(i), LocalDate.parse(args[1]),PortfolioManagerApplication.getToken());
        AnnualizedReturn annReturn  = calculateAnnualizedReturns(candles.get(candles.size()-1).getDate(), pma.get(i), getOpeningPriceOnStartDate(candles), getClosingPriceOnEndDate(candles));
        annualList.add(annReturn);
      }
      Collections.sort(annualList,new Comparator<AnnualizedReturn>() {
        @Override

        public int compare(AnnualizedReturn arg0,AnnualizedReturn arg1){
        return(arg0.getAnnualizedReturn()>arg1.getAnnualizedReturn())?-1:(arg0.getAnnualizedReturn()<arg1.getAnnualizedReturn())?1:0;
        }
        
      });

      return annualList;
 }



 public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,
     PortfolioTrade trade, Double buyPrice, Double sellPrice) {
      Double total_Return = (sellPrice-buyPrice)/buyPrice;
      double days = ChronoUnit.DAYS.between(trade.getPurchaseDate(), endDate);
      Double totalYear = (days/365);
      Double annual  = Math.pow(1+total_Return,1/totalYear)-1;
     return new AnnualizedReturn(trade.getSymbol(),annual , total_Return);
 }


  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Once you are done with the implementation inside PortfolioManagerImpl and
  //  PortfolioManagerFactory, create PortfolioManager using PortfolioManagerFactory.
  //  Refer to the code from previous modules to get the List<PortfolioTrades> and endDate, and
  //  call the newly implemented method in PortfolioManager to calculate the annualized returns.

  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.

  public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args)
      throws Exception {

       String file = args[0];
       LocalDate endDate = LocalDate.parse(args[1]);
       String contents = readFileAsString(file);
       ObjectMapper objectMapper = getObjectMapper();
       PortfolioTrade[] portfolioTrades = objectMapper.readValue(contents, PortfolioTrade[].class);
       return portfolioManager.calculateAnnualizedReturn(Arrays.asList(portfolioTrades), endDate);
  }

  private static String readFileAsString(String fileName) throws IOException, URISyntaxException {
    return new String(Files.readAllBytes(resolveFileFromResources(fileName).toPath()), "UTF-8");
  }
  






  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());
    



    printJsonObject(mainReadFile(args));

    printJsonObject(mainCalculateSingleReturn(args));

    printJsonObject(mainReadQuotes(args));

    printJsonObject(mainCalculateReturnsAfterRefactor(args));
  }
}

