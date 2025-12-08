package ESINF.controller;

import ESINF.model.HubCentralityResult;
import ESINF.model.RailwayStation;
import ESINF.repository.RailwayNetworkRepository;
import ESINF.service.CentralityAnalysisService;
import Graph.Graph;

import java.io.IOException;
import java.util.List;

/**
 * Controlador para gerir o cálculo e apresentação da Análise de Centralidade de Hub Ferroviário (USEI13).
 */
public class CentralityController {

    private final CentralityAnalysisService analysisService;
    private final RailwayNetworkRepository networkRepository;

    public CentralityController(RailwayNetworkRepository networkRepository) {
        this.networkRepository = networkRepository;
        // O serviço é instanciado aqui, recebendo o repositório
        this.analysisService = new CentralityAnalysisService(networkRepository);
    }

    /**
     * Carrega a rede ferroviária belga a partir de ficheiros CSV.
     * @param stationsPath caminho para stations.csv
     * @param linesPath caminho para lines.csv
     * @return LoadResult com estatísticas
     * @throws IOException se houver erro ao ler os ficheiros
     */
    public RailwayNetworkRepository.LoadResult loadRailwayNetwork(String stationsPath, String linesPath) throws IOException {
        return networkRepository.loadFromCSV(stationsPath, linesPath);
    }

    /**
     * Verifica se os dados estão carregados.
     * @return true se os dados estão carregados
     */
    public boolean isDataLoaded() {
        return networkRepository.isDataLoaded();
    }

    /**
     * Executa a Análise de Centralidade de Hub Ferroviário (USEI13).
     *
     * @return Uma lista de HubCentralityResult ordenada pelo HubScore descendente.
     */
    public List<HubCentralityResult> analyzeRailHubCentrality() {
        // Obtém o grafo diretamente do repositório injetado
        Graph<RailwayStation, Double> graph = this.networkRepository.getRailNetworkWithDistanceAsWeight();

        return analysisService.computeHubCentrality(graph);
    }

    /**
     * Retorna a string de complexidade temporal.
     * @return A análise de complexidade.
     */
    public String getTemporalComplexity() {
        return analysisService.getTemporalComplexityAnalysis();
    }
}