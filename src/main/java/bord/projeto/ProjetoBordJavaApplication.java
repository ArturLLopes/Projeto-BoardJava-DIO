package bord.projeto;

import bord.projeto.persistence.migration.MigrationStrategy;
import bord.projeto.ui.MainMenu;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.SQLException;

import static bord.projeto.persistence.ConnectionConfig.getConnection;

@SpringBootApplication
public class ProjetoBordJavaApplication {

	public static void main(String[] args) throws SQLException {


		try (var connection = getConnection()) {
			new MigrationStrategy(connection).executeMigration();
		}
		new MainMenu().execute();
	}

}

