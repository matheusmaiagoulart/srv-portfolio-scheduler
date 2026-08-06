package com.matheus.srv_portfolio_scheduler.adapters.output.commands;

import com.matheus.srv_portfolio_scheduler.application.dto.QuoteDTO;
import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.CotahistFilePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Component
public class CotahistFileAdapter implements CotahistFilePort {

    @Value("${app.cotahist.path}")
    private String cotahistPath;

    @Override
    public List<QuoteDTO> parse(Set<String> tickers, String pathFile) {
        List<QuoteDTO> quotes = new ArrayList<>();

        Path path = Paths.get(pathFile);

        try {
            List<String> lines = Files.readAllLines(path);

            for (String line : lines) {
                if (line.length() < 245) continue;

                String tipoRegistro = line.substring(0, 2);
                if (!tipoRegistro.equals("01")) continue;

                int tipoMercado = Integer.parseInt(line.substring(24, 27).trim());
                if (tipoMercado != 10 && tipoMercado != 20) continue;

                String bdi = line.substring(10, 12).trim();
                String ticker = line.substring(12, 24).trim();

                if (bdi.equals("96")) {
                    String tickerSemF = ticker.substring(0, ticker.length() - 1);
                    if (!tickers.contains(tickerSemF)) continue;
                } else {
                    if (!tickers.contains(ticker)) continue;
                }

                QuoteDTO quote = new QuoteDTO(
                        parseDate(line.substring(2, 10)),
                        ticker,
                        bdi,
                        tipoMercado,
                        line.substring(27, 39).trim(),
                        parsePrice(line.substring(56, 69)),
                        parsePrice(line.substring(69, 82)),
                        parsePrice(line.substring(82, 95)),
                        parsePrice(line.substring(108, 121)),
                        parsePrice(line.substring(95, 108)),
                        Long.parseLong(line.substring(152, 170).trim()),
                        parsePrice(line.substring(170, 188)));

                quotes.add(quote);
            }
        } catch (IOException e) {
            log.error("Error reading cotahist file: {}", pathFile, e);
            throw new RuntimeException("Failed to read cotahist file", e);
        }

        return quotes;
    }

    private BigDecimal parsePrice(String price) {
        BigDecimal value = BigDecimal.valueOf(Long.parseLong(price.trim()));
        return value.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private LocalDate parseDate (String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return LocalDate.parse(date, formatter);
    }

    @Override
    public List<QuoteDTO> getTickerByLastCotahist(Set<String> tickers) {

        try (var stream = Files.list(Paths.get(cotahistPath))) {
            Optional<Path> latestFile = stream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toUpperCase().endsWith(".TXT"))
                    .max((p1, p2) -> {
                        try {
                            return Files.getLastModifiedTime(p1).compareTo(Files.getLastModifiedTime(p2));
                        } catch (IOException e) {
                            throw new RuntimeException("Erro ao comparar data de modificacao dos arquivos", e);
                        }
                    });

            if (latestFile.isEmpty()) {
                log.warn("Nenhum arquivo .TXT encontrado em {}", cotahistPath);
                return List.of();
            }

            return parse(tickers, latestFile.get().toString());
        } catch (IOException e) {
            log.error("Erro ao listar arquivos em {}", cotahistPath, e);
            throw new RuntimeException("Falha ao buscar ultimo arquivo cotahist", e);
        }
    }

    @Override
    public Optional<String> existsCotahistFile(LocalDate referenceDate) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
        String date = referenceDate.format(formatter);
        String caminhoArquivo = cotahistPath
                .concat(date).concat(".TXT");

        Path path = Paths.get(caminhoArquivo);
        if (path.toFile().exists()) {
            return Optional.of(caminhoArquivo);
        } else {
            return Optional.empty();
        }
    }
}
