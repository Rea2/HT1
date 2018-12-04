package app;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ManagePersonServlet extends HttpServlet {
	
	private RequestDispatcher dispatcher_for_manager =  null; 	
    private RequestDispatcher dispatcher_for_list = null;
    private RequestDispatcher dispatcher_for_phoneeditor =  null;
    private HashMap<String,String> jsp_parameters = null; 
	
	// Идентификатор для сериализации/десериализации.
	private static final long serialVersionUID = 1L;
	
	// Основной объект, хранящий данные телефонной книги.
	private Phonebook phonebook;
       
    public ManagePersonServlet()
    {	
    	// Создание экземпляра телефонной книги.
        try
		{
			this.phonebook = Phonebook.getInstance();
		}
		catch (ClassNotFoundException | SQLException e )
		{
			e.printStackTrace();
		}	
    } 
    
    // Установка общих аттрибутов для запроса и создание диспетчеров для передачи управления на разные JSP
    private void beforeDoMethod (HttpServletRequest req)
            throws ServletException, IOException 
    {    	
	     // Обязательно ДО обращения к любому параметру нужно переключиться в UTF-8,
	     // иначе русский язык при передаче GET/POST-параметрами превращается в "кракозябры".
        req.setCharacterEncoding("UTF-8");
        
		// В JSP нам понадобится сама телефонная книга. Можно создать её экземпляр там,
		// но с архитектурной точки зрения логичнее создать его в сервлете и передать в JSP.
		req.setAttribute("phonebook", this.phonebook);
		
		// Хранилище параметров для передачи в JSP.
		jsp_parameters = new HashMap<String,String>();
		
		// Диспетчеры для передачи управления на разные JSP (разные представления (view)).
				dispatcher_for_manager = req.getRequestDispatcher("/ManagePerson.jsp");	
		        dispatcher_for_list = req.getRequestDispatcher("/List.jsp");
		        dispatcher_for_phoneeditor = req.getRequestDispatcher("/PhoneEditor.jsp");  
    }
    
    // Установка параметров "current_action", "next_action", "next_action_label"  для передачи в  jsp файл
    private void setJspActionParameters (String current_action, String next_action , String next_action_label)
    {
    	jsp_parameters.put("current_action", current_action);
		jsp_parameters.put("next_action", next_action);
		jsp_parameters.put("next_action_label", next_action_label);
    }    
    
    // Реакция на GET-запросы.
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{  
		 // Установка аттрибутов для запроса и создание диспетчеров
		beforeDoMethod( request);

		// Действие (action) и идентификатор записи (id) над которой выполняется это действие.
		String action = request.getParameter("action");
		String id = request.getParameter("id");
		String id_phone = request.getParameter("id_phone");
				
		// Если идентификатор и действие не указаны, мы находимся в состоянии
		// "просто показать список и больше ничего не делать".
        if ((action == null)&&(id == null)&&(id_phone == null))
        {
        	request.setAttribute("jsp_parameters", jsp_parameters);
            dispatcher_for_list.forward(request, response);
        }
        // Если же действие указано, то...
        else
        {
        	switch (action)
        	{
        		// Добавление записи.
        		case "add":
        			// Создание новой пустой записи о пользователе.
        			Person empty_person = new Person();
        			
        			// Подготовка параметров для JSP.
        			setJspActionParameters("add","add_go","Добавить");       			
        			
        			// Установка параметров JSP.
        			request.setAttribute("person", empty_person);
        			request.setAttribute("jsp_parameters", jsp_parameters);
        			
        			// Передача запроса в JSP.
        			dispatcher_for_manager.forward(request, response);
        		break;
        		case "add_phone":
        			// Получение пользователя:
        			Person person = this.phonebook.getPerson(id);
        			
        			// Подготовка параметров для JSP.
        			setJspActionParameters("add_phone","add_phone_go","Добавить"); 
        			
        			// Установка параметров JSP.
        			request.setAttribute("person", person);        
        			request.setAttribute("jsp_parameters", jsp_parameters);
        			
        			// Передача запроса в JSP.      			
        			dispatcher_for_phoneeditor.forward(request, response);
        		break;
			
			
        		// Редактирование записи.
        		case "edit":
        			// Извлечение из телефонной книги информации о редактируемой записи.        			
        			Person editable_person = this.phonebook.getPerson(id);
        			
        			// Подготовка параметров для JSP.
        			setJspActionParameters("edit","edit_go","Сохранить");         	

        			// Установка параметров JSP.
        			request.setAttribute("person", editable_person);
        			request.setAttribute("jsp_parameters", jsp_parameters);
        			
        			// Передача запроса в JSP.
        			dispatcher_for_manager.forward(request, response);
        		break;
       		case "edit_phone":
        			// Извлечение из телефонной книги информации о редактируемой записи.
        			Person person_with_editable_phone = this.phonebook.getPerson(id);
        			
        			// Подготовка параметров для JSP.
        			setJspActionParameters("edit_phone","edit_phone_go","Сохранить");   
        			
        			// Установка параметров JSP.
        			request.setAttribute("id_phone", id_phone);
        			request.setAttribute("person", person_with_editable_phone);
        			request.setAttribute("jsp_parameters", jsp_parameters);
        			
        			// Передача запроса в JSP.
        			dispatcher_for_phoneeditor.forward(request, response);
        		break;
		
        		// Удаление записи.
        		case "delete":
        			
        			// Если запись удалось удалить...
        			if (phonebook.deletePerson(id))
        			{
        				jsp_parameters.put("current_action_result", "DELETION_SUCCESS");
        				jsp_parameters.put("current_action_result_label", "Удаление выполнено успешно");
        			}
        			// Если запись не удалось удалить (например, такой записи нет)...
        			else
        			{
        				jsp_parameters.put("current_action_result", "DELETION_FAILURE");
        				jsp_parameters.put("current_action_result_label", "Ошибка удаления (возможно, запись не найдена)");
        			}

        			// Установка параметров JSP.
        			request.setAttribute("jsp_parameters", jsp_parameters);       			
        			
        			// Передача запроса в JSP.
        			dispatcher_for_list.forward(request, response);
       			break;
       		  // Удаление телефона.
    			case "delete_phone":
        			
        			// Если запись удалось удалить...
        			if (phonebook.deletePhone(id, id_phone))
        			{
        				jsp_parameters.put("current_action_result", "DELETION_SUCCESS");
        				jsp_parameters.put("current_action_result_label", "Удаление выполнено успешно");
        			}
        			// Если запись не удалось удалить (например, такой записи нет)...
        			else
        			{
        				jsp_parameters.put("current_action_result", "DELETION_FAILURE");
        				jsp_parameters.put("current_action_result_label", "Ошибка удаления (возможно, запись не найдена)");
        				
        			}

        			// Установка параметров JSP.
        			setJspActionParameters("add_phone_go", "edit_go", "Сохранить");
        			request.setAttribute("jsp_parameters", jsp_parameters);
        			request.setAttribute("person", phonebook.getPerson(id));
        			
        			// Передача запроса в JSP.
        			dispatcher_for_manager.forward(request, response);
       			break;
       		}
        }
		
	}

	// Реакция на POST-запросы.
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		 // Установка аттрибутов для запроса и создание диспетчеров
		beforeDoMethod( request);
		
		// Действие (add_go, edit_go) и идентификатор записи (id) над которой выполняется это действие.
		String add_go = request.getParameter("add_go");
		String edit_go = request.getParameter("edit_go");
		String add_phone_go = request.getParameter("add_phone_go");
		String edit_phone_go = request.getParameter("edit_phone_go");
		String id = request.getParameter("id");
		String id_phone = request.getParameter("id_phone");				
		
		// Добавление записи.
		if (add_go != null)
		{
			// Создание записи на основе данных из формы.
			Person new_person = new Person(request.getParameter("name"), request.getParameter("surname"), request.getParameter("middlename"));

			// Валидация ФИО.
			String error_message = new_person.validatethisFMLName(); 
			
			// Если данные верные, можно производить добавление.
			if (error_message.equals(""))
			{

				// Если запись удалось добавить...
				if (this.phonebook.addPerson(new_person))
				{
					jsp_parameters.put("current_action_result", "ADDITION_SUCCESS");
					jsp_parameters.put("current_action_result_label", "Добавление выполнено успешно");
				}
				// Если запись НЕ удалось добавить...
				else
				{
					jsp_parameters.put("current_action_result", "ADDITION_FAILURE");
					jsp_parameters.put("current_action_result_label", "Ошибка добавления");
				}

				// Установка параметров JSP.
				request.setAttribute("jsp_parameters", jsp_parameters);
	        
				// Передача запроса в JSP.
				dispatcher_for_list.forward(request, response);
			}
			// Если в данных были ошибки, надо заново показать форму и сообщить об ошибках.
			else
			{
    			// Подготовка параметров для JSP.
				setJspActionParameters("add", "add_go",  "Добавить");    			
    			jsp_parameters.put("error_message", error_message);
    			
    			// Установка параметров JSP.
    			request.setAttribute("person", new_person);
    			request.setAttribute("jsp_parameters", jsp_parameters);
    			
    			// Передача запроса в JSP.
    			dispatcher_for_manager.forward(request, response);
			}
		}		
		// Добавление записи.
		if (add_phone_go != null)
		{
			// Создание записи на основе данных из формы.
			Person editable_person = this.phonebook.getPerson(id);
				
			// Валидация ФИО.
			String error_message = Person.validatePhoneNumber(request.getParameter("phone")); 	

			// Если данные верные, можно производить добавление.
			if (error_message.equals(""))
			{
				// Если запись удалось добавить...
				if (this.phonebook.addPhone(editable_person, request.getParameter("phone")))
				{
					jsp_parameters.put("current_action_result", "ADDITION_SUCCESS");
					jsp_parameters.put("current_action_result_label", "Добавление выполнено успешно");
				}
				// Если запись НЕ удалось добавить...
				else
				{
					jsp_parameters.put("current_action_result", "ADDITION_FAILURE");
					jsp_parameters.put("current_action_result_label", "Ошибка добавления");				
				}

				// Установка параметров JSP.
				setJspActionParameters( "add_phone_go", "edit_go",  "Сохранить");
				request.setAttribute("person", editable_person);
				request.setAttribute("jsp_parameters", jsp_parameters);
				
	        
				// Передача запроса в JSP.
				dispatcher_for_manager.forward(request, response);
			}
			// Если в данных были ошибки, надо заново показать форму и сообщить об ошибках.
			else
			{
    			// Подготовка параметров для JSP.
				setJspActionParameters( "add_phone", "add_phone_go", "Добавить");				
    			jsp_parameters.put("error_message", error_message);
    			
    			// Установка параметров JSP.
    			request.setAttribute("person", editable_person);
    			request.setAttribute("jsp_parameters", jsp_parameters);
    			
    			// Передача запроса в JSP.
    			dispatcher_for_phoneeditor.forward(request, response);
			}
		}
		
		// Редактирование записи.
		if (edit_go != null)
		{
			// Получение записи и её обновление на основе данных из формы.
			Person updatable_person = this.phonebook.getPerson(request.getParameter("id")); 
			updatable_person.setName(request.getParameter("name"));
			updatable_person.setSurname(request.getParameter("surname"));
			updatable_person.setMiddlename(request.getParameter("middlename"));

			// Валидация ФИО.
			String error_message = updatable_person.validatethisFMLName(); 
			
			// Если данные верные, можно производить добавление.
			if (error_message.equals(""))
			{
			
				// Если запись удалось обновить...
				if (this.phonebook.updatePerson(id, updatable_person))
				{
					jsp_parameters.put("current_action_result", "UPDATE_SUCCESS");
					jsp_parameters.put("current_action_result_label", "Обновление выполнено успешно");
				}
				// Если запись НЕ удалось обновить...
				else
				{ 
					jsp_parameters.put("current_action_result", "UPDATE_FAILURE");
					jsp_parameters.put("current_action_result_label", "Ошибка обновления");
				}

				// Установка параметров JSP.
				request.setAttribute("jsp_parameters", jsp_parameters);
	        
				// Передача запроса в JSP.
				dispatcher_for_list.forward(request, response);
			}
			// Если в данных были ошибки, надо заново показать форму и сообщить об ошибках.
			else
			{

    			// Подготовка параметров для JSP.
				setJspActionParameters( "edit", "edit_go", "Сохранить");	
    			jsp_parameters.put("error_message", error_message);

    			// Установка параметров JSP.
    			request.setAttribute("person", updatable_person);
    			request.setAttribute("jsp_parameters", jsp_parameters);
    			
    			// Передача запроса в JSP.
    			dispatcher_for_manager.forward(request, response);    			
    			
			}
		}
		
		// Редактирование телефонной записи.
			if (edit_phone_go != null)
				{
					// Получение записи и её обновление на основе данных из формы.
					Person updatable_person = this.phonebook.getPerson(request.getParameter("id")); 				
					String edited_number = request.getParameter("phone");	

					// Валидация телефона.
					String error_message = Person.validatePhoneNumber(edited_number); 
					
					if (error_message.equals(""))
					{					
						// Если запись удалось обновить...
						if (this.phonebook.updatePhone(id,id_phone, edited_number))
						{
							jsp_parameters.put("current_action_result", "UPDATE_SUCCESS");
							jsp_parameters.put("current_action_result_label", "Обновление выполнено успешно");
						}
						// Если запись НЕ удалось обновить...
						else
						{ 
							jsp_parameters.put("current_action_result", "UPDATE_FAILURE");
							jsp_parameters.put("current_action_result_label", "Ошибка обновления");
							
						}						

						// Установка параметров JSP.
						setJspActionParameters( "edit_phone_go", "edit_go", "Сохранить");								
			   			request.setAttribute("person", updatable_person);	
						request.setAttribute("jsp_parameters", jsp_parameters);
			        
						// Передача запроса в JSP.
						dispatcher_for_manager.forward(request, response);
					}
					// Если в данных были ошибки, надо заново показать форму и сообщить об ошибках.
					else
					{

		    			// Подготовка параметров для JSP.
						setJspActionParameters( "edit_phone_go", "edit_phone_go", "Сохранить");		
		    			jsp_parameters.put("error_message", error_message);

		    			// Установка параметров JSP.
		    			request.setAttribute("person", updatable_person);
		    			request.setAttribute("jsp_parameters", jsp_parameters);
		    			request.setAttribute("phone", edited_number);
		    			request.setAttribute("id_phone", id_phone);
		    			
		    			// Передача запроса в JSP.
		    			dispatcher_for_phoneeditor.forward(request, response);    					    			
					}					
					
				}
		}
}
