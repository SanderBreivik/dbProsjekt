package ui;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.ZoneId;
import java.sql.PreparedStatement;
import db_connection.ConnectService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class ResultController {

	private ConnectService cs = new ConnectService();

	@FXML
	private DatePicker fromDate;

	@FXML
	private DatePicker toDate;

	@FXML
	private TextArea data;

	@FXML
	private Button submit;

	@FXML
	private Button back;

	@FXML
	private ChoiceBox<String> ex;

	public void initialize() throws SQLException {
		ObservableList<String> exercises = FXCollections.observableArrayList();
		String query = "SELECT navn FROM `øvelse`";
		try (Connection conn = cs.getConnection();
				Statement stm = conn.createStatement();
				ResultSet rs = stm.executeQuery(query)) {
			while (rs.next()) {
				exercises.add(rs.getString("navn"));
			}
			ex.setItems(exercises);
			ex.setValue(ex.getItems().get(0));
		}
	}

	public Date getDate(DatePicker date) {
		return Date.valueOf(date.getValue());
	}

	public void getData() throws Exception {
		String query = "SELECT * FROM `øvelse` NATURAL JOIN `treningsøkt` NATURAL JOIN utførte_øvelse WHERE dato_tidspunkt > (?) < (?) and navn = (?)";
		String query2 = "SELECT COUNT(*) AS `Antall` from treningsøkt WHERE dato_tidspunkt > (?) < (?);";
		String query3 = "SELECT * FROM `øvelse` NATURAL JOIN `treningsøkt` NATURAL JOIN apparatøvelse_i_treningsøkt WHERE øvelse.navn = (?) AND treningsøkt_id = (?) and dato_tidspunkt >= (?) <= (?);";
		try (Connection conn = cs.getConnection();
				PreparedStatement pstm = conn.prepareStatement(query);
				PreparedStatement pstm2 = conn.prepareStatement(query2);
				PreparedStatement pstm3 = conn.prepareStatement(query3)) {
			Date fDate = getDate(fromDate);
			Date tDate = getDate(toDate);
			String exercise = ex.getSelectionModel().getSelectedItem();
			pstm.setDate(1, fDate);
			pstm.setDate(2, tDate);
			pstm.setString(3, exercise);
			pstm2.setDate(1, fDate);
			pstm2.setDate(2, tDate);
			System.out.println(pstm);
			System.out.println(pstm2);
			try (ResultSet rs = pstm.executeQuery(); ResultSet rs2 = pstm2.executeQuery()) {
				String str = "";
				while (rs2.next()) {
					str += "Antall økter i perioden: " + rs2.getInt("Antall") + "\n";
					str += "===================================\n";
				}
				while (rs.next()) {
					String notat = rs.getString("notat");
					String type = rs.getString("øvelse_type");
					String navn = rs.getString("navn");
					if (notat == null) {
						notat = "Ingen notat";
					}
					str += "Navn: " + rs.getString("navn") + "\n";
					str += "Øvelse type: " + type + "\n";
					str += "Dato: " + rs.getDate("dato_tidspunkt") + "\n";
					str += "Varighet: " + rs.getTime("varighet") + "\n";
					str += "Form: " + rs.getInt("form") + "\n";
					str += "Prestasjon: " + rs.getInt("prestasjon") + "\n";
					str += "Notat: " + notat + "\n";
					if (type.equalsIgnoreCase("apparatøvelse")) {
						pstm3.setString(1, navn);
						pstm3.setInt(2, rs.getInt("treningsøkt_id"));
						pstm3.setDate(3, fDate);
						pstm3.setDate(4, tDate);
						System.out.println(pstm3);
						try (ResultSet rs3 = pstm3.executeQuery()) {
							while (rs3.next()) {
								str += "Antall sett: " + rs3.getString("antall_sett") + "\n";
								str += "Antall kilo: " + rs3.getString("antall_kilo") + "\n";
							}
						}
					}
					;

					str += "-----------------------------------\n";
					data.setText(str);
				}
			} catch (Exception e) {
				Alerter.error("Ugyldig valg", "Vennligst sjekk dato og valg av øvelse");
				e.printStackTrace();
			}
		}
	}

	public void toBack() {
		try {
			Stage stage = (Stage) back.getScene().getWindow();
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Menu.fxml"));
			Parent root1 = (Parent) fxmlLoader.load();
			stage.setScene(new Scene(root1));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
