
package com.crio.warmup.stock.portfolio;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.quotes.StockQuotesService;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.crio.warmup.stock.quotes.StockQuotesService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {




  // private RestTemplate restTemplate;



  private static final Double NaN = null;
  protected RestTemplate restTemplate;
private StockQuotesService stockQuotesService;


  // Caution: Do not delete or modify the constructor, or else your build will break!
  // This is absolutely necessary for backward compatibility
  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }
  PortfolioManagerImpl(StockQuotesService stockQuotesService){
    this.stockQuotesService = stockQuotesService;
  }


  

  //TODO: CRIO_TASK_MODULE_REFACTOR
  // 1. Now we want to convert our code into a module, so we will not call it from main anymore.
  //    Copy your code from Module#3 PortfolioManagerApplication#calculateAnnualizedReturn
  //    into #calculateAnnualizedReturn function here and ensure it follows the method signature.
  // 2. Logic to read Json file and convert them into Objects will not be required further as our
  //    clients will take care of it, going forward.

  // Note:
  // Make sure to exercise the tests inside PortfolioManagerTest using command below:
  // ./gradlew test --tests PortfolioManagerTest

  //CHECKSTYLE:OFF




  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }

  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Extract the logic to call Tiingo third-party APIs to a separate function.
  //  Remember to fill out the buildUri function and use that.
  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    String Token = "20fc3ace9581a5beb1594642fee6a83d67644b07";
      //9104fffb31297d3db64a2fc69778ba110486b078
    String url = "https://api.tiingo.com/tiingo/daily/" + symbol+ "/prices?startDate="
    + startDate + "&endDate=" + endDate.toString()
    + "&token="+Token;
    return url;
  }

  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException, StockQuoteServiceException {
       return stockQuotesService.getStockQuote(symbol, from, to);
  }

  

public AnnualizedReturn getAnnualReturn(PortfolioTrade trade,LocalDate endDate) throws StockQuoteServiceException{
  AnnualizedReturn annualizedReturn;
  try {
    List<Candle>candle = getStockQuote(trade.getSymbol(), trade.getPurchaseDate(), endDate);
    Double buyPrice = candle.get(0).getOpen();
  Double sellPrice = candle.get(candle.size()-1).getClose();

  Double totalReturn  = (sellPrice-buyPrice)/buyPrice;
  double days = ChronoUnit.DAYS.between(trade.getPurchaseDate(), endDate);
  Double totalYear = (days/365);
  Double annual  = Math.pow(1+totalReturn,1/totalYear)-1;
  annualizedReturn = new AnnualizedReturn(trade.getSymbol(), annual, totalReturn);

  } catch (JsonProcessingException e) {
    // TODO Auto-generated catch block
    annualizedReturn = new AnnualizedReturn(trade.getSymbol(),NaN,NaN);
  }
  return annualizedReturn;

}


public  List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades,
        LocalDate endDate) throws StockQuoteServiceException {
          AnnualizedReturn annualReturn;
          List<AnnualizedReturn>annualReturns = new ArrayList<>();
          for(int i =0;i<portfolioTrades.size();i++){
            annualReturn = getAnnualReturn(portfolioTrades.get(i),endDate);
            annualReturns.add(annualReturn);
  
          }
          Collections.sort(annualReturns,getComparator());
          return annualReturns;
}
 
 

 
}
