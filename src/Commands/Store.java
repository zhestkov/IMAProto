package Commands;

import java.io.PrintWriter;
import java.util.Vector;

import architecture.DB;

public class Store extends Command {
	
	private String request;
	private DB db;
	private String unmarked;
	private String marked;
	private boolean plus = false;
	private PrintWriter out;
	private String[] FLAGS = null;
	
	// TO DO: sequence checking(e.g. STORE 2:4 ...)
	public Store (String prefix, String request, DB db, PrintWriter out) {
        this.FLAGS = new String[]{"Answered", "Recent", "Deleted", "Seen", "Draft", "Flagged"};
		this.prefix = prefix;
        this.request = request;
        this.db = db;
        this.out = out;
    }

	
	 // Выполнение комманды Сохранение письма в БД 

	public void execute() {
		GetPluse();
		String UID = GetUID();
		Vector<String> flags = GetFlags();

		unmarked = makeUnmarked(UID, flags);
		marked = makeMarked(UID, flags);

		sendResponse(unmarked, out);
		sendResponse(marked, out);

	}

	/**
	 * Проверка наличия флага "+" в запросе
	 */
	public void GetPluse() {
		if (request.contains("+")) {
			plus = true;
		} else {
			plus = false;
		}
	}

	
	 // Получение UID письма
	public String GetUID() {
		String UID = "";
		int startSymbol = request.indexOf('E') + 2;
		int endSymbol;

		if (plus) {
			endSymbol = request.indexOf('+') - 2;
		} else {
			endSymbol = request.indexOf('-') - 2;
		}

		for (int i = startSymbol; i <= endSymbol; i++) {
			UID += request.charAt(i);
		}

		return UID;
	}

	// Получения флагов письма

	public Vector<String> GetFlags() {
		Vector<String> flags = new Vector<String>();

		for (int i = 0; i < FLAGS.length; i++) {
			if (request.contains(FLAGS[i])) {
				flags.add(FLAGS[i]);
			}
		}

		return flags;
	}

	// Добавляем флаги сообщения в БД
	public int ChangeFlag(String UID, String flag) {
		//String uppercaseFlag = flag.toUpperCase();
		int result;
		if (plus) {
			result = db.addFlagToMessage(UID, flag);
		} else {
			result = db.removeFlagFromMessage(UID, flag);
		}

		return result;
	}

	// Собираем строку ответа без префикса
	public String makeUnmarked(String UID, Vector<String> flags) {
		String response = ("* STORE " + UID + " FLAGS (");
		for (int i = 0; i < flags.size(); i++) {
			response += "\\" + flags.get(i) + " ";
		}
		response = response.substring(0, response.length() - 1);
		response += ")";

		return response;
	}

	// Собираем строку ответа с префиксом
	public String makeMarked(String UID, Vector<String> flags) {
		String response = "";

		for (int i = 0; i < flags.size(); i++) {
			int result = ChangeFlag(UID, flags.get(i));
			if (result == 1) {
				response = prefix + " OK STORE completed";
			} else {
				response = prefix + " NO STORE can't complete";
			}
		}
		return response;
	}
}
