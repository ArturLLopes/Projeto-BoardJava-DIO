package bord.projeto.persistence.migration;


import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.AllArgsConstructor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;

import static bord.projeto.persistence.ConnectionConfig.getConnection;

@AllArgsConstructor
public class MigrationStrategy {

    private final Connection connection;  // Conexão com o banco de dados

    /**
     * Executa a migração do banco de dados usando o Liquibase.
     * Durante a execução, os fluxos de saída padrão são redirecionados para um arquivo de log.
     */
    public void executeMigration() {
        var originalOut = System.out;  // Armazena o fluxo original de saída padrão
        var originalErr = System.err;  // Armazena o fluxo original de erro padrão

        try (var fos = new FileOutputStream("liquibase.log")) {  // Abre o arquivo de log
            // Redireciona as saídas padrão e de erro para o arquivo
            System.setOut(new PrintStream(fos));
            System.setErr(new PrintStream(fos));

            // Estabelece a conexão com o banco de dados e inicializa o Liquibase
            try (var connection = getConnection();  // Conexão com o banco de dados
                 var jdbcConnection = new JdbcConnection(connection)) {

                // Inicializa o Liquibase com o arquivo de changelog
                var liquibase = new Liquibase(
                        "/db/changelog/db.changelog-master.yml",
                        new ClassLoaderResourceAccessor(),
                        jdbcConnection);

                // Executa a migração
                liquibase.update();
            } catch (SQLException | LiquibaseException e) {
                // Trata exceções de conexão com o banco e do Liquibase
                e.printStackTrace();
                System.setErr(originalErr);  // Restaura o fluxo de erro padrão após exceção
            }
        } catch (IOException ex) {
            // Trata exceções de entrada/saída (erro ao criar ou escrever no arquivo de log)
            ex.printStackTrace();
        } finally {
            // Restaura os fluxos originais de saída e erro, independentemente do que aconteça
            System.setOut(originalOut);
            System.setErr(originalErr);
        }
    }

    /**
     * Obtém a conexão com o banco de dados.
     * Este método precisa ser implementado ou configurado para fornecer a conexão adequada.
     *
     * @return A conexão com o banco de dados.
     * @throws SQLException Se houver um erro ao estabelecer a conexão.
     */
    private Connection getConnection() throws SQLException {
        // Implementação para obter a conexão com o banco de dados
        return connection;  // Aqui você pode retornar a conexão configurada em outro local.
    }
}
