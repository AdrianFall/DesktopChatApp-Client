import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import javax.swing.*;


public class ClientMain {
	
	private static InetAddress host;
	private static int portNumber = 4444;
	private static String userName;
	private static Scanner networkInput;
	private static PrintWriter networkOutput;
	private static Socket link = null;
	private static JFrame window;
	private static JTextArea chatArea;
	private static JTextField userNameInputField;
	private static JButton connectButton;
	private static JButton disconnectButton;
	private static JButton sendMessageButton;
	//Declare a boolean for the automatic scrolling of the chat area
	private static boolean automaticScrolling = true;
	//Declare a JList for displaying the users online
	private static JList<String> onlineListArea;
	//Declare a default list model for adding/removing etc the elements of JList
	private static DefaultListModel<String> model;
	//Instantiate an array list for storing/retrieving/manipulating the names of users chatting with (i.e. online list)
	private static ArrayList<String> usersChattingWith = new ArrayList<String>();
	//Instantiate an array list for storing/retrieving/manipulating the private chat windows
	private static ArrayList<JFrame> privChatWindows = new ArrayList<JFrame>();
	//Instantiate an array list for storing/retrieving/manipulating the private chat areas
	private static ArrayList<JTextArea> privChatAreas = new ArrayList<JTextArea>();
	//Declare a variable for storing the modified online list
	private static String modifiedOnlineList;
	//Instantiate an array list for storing/retrieving/manipulating the names of the users that have been requested for private chat
	private static ArrayList<String> userRequestedForPrivChat = new ArrayList<String>();
	
	/**
	 * The constructor of this class creates the GUI with all the functionality 
	 * for its components and action listeners 
	 */
	public ClientMain() {
		//Create an instance of the JFrame object
		window = new JFrame("Chat");
		//Add an action listener to the window, so it allows for custom behaviour when closing the window
		window.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				//If the link is still up/exists
				if (link != null) {
					try {
						//Loop through the users chatting with (i.e. private chats)
						for (int i = 0; i < usersChattingWith.size(); i++) {
							//Announce the closure of the private chat with the currently looped user, so the other user can notice it
			    			announceClosureOfPrivChat(usersChattingWith.get(i));
			    			//Call a method to close the private chat with the currently looped user chatting with
			    			closePrivChat(usersChattingWith.get(i));
						}
						//Call a method to disconnect from the server
						disconnectFromServer();
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(window, "Could not disconnect from the server.", "Disconnect error", JOptionPane.ERROR_MESSAGE);
						e1.printStackTrace();
					}
					System.exit(0);
				}//End of if the link is still up/exists
			}//End of windowClosing method
		});//End of action listener for window
		
		//Set the default operation when user closes the window (frame)
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//Set the size of the window
		window.setSize(800, 600);
		//Do not allow resizing of the window
		window.setResizable(false);
		//Set the position of the window to be in middle of the screen when program is started
		window.setLocationRelativeTo(null);
		
		//Call the setUpWindow method for setting up all the components needed in the window
		window = setUpWindow(window);
		
		//Set the window to be visible
		window.setVisible(true);
	}
	/**
	 * The main method of this class
	 * @param args - For example for the command line arguments when running the java class
	 */
	public static void main (String[] args) {
		
		//Initialise the constructor of this class
		new ClientMain();
		
		
		try {
			//Obtain the host's local IP Address
			host = InetAddress.getLocalHost();
			
		} catch (UnknownHostException e) {
			//Show an error message dialog
			JOptionPane.showMessageDialog(window, "Host could not be resolved.", "Host error", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
		
		//Loop for the incoming responses from the server
		while(true) {
			try {
				TimeUnit.MILLISECONDS.sleep(600);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//Call the listenToServer method, to listen to incoming messages
			listenToServer();
		}
		
	}//End of main
	
	/**
	 * Creates a method for setting up the window, i.e. its layout, components and their positions,
	 *  action and mouse listeners.
	 * @param window - The JFrame of the chat window 
	 * @return - returns a JFrame
	 */
	//Create a method for setting up the window
    private static JFrame setUpWindow(final JFrame window) {
    	//Create an instance of the JPanel object
    	JPanel panel = new JPanel();
    	//Set the panel's layout manager to null
    	panel.setLayout(null);
    	
    	//Set the bounds of the window
    	panel.setBounds(0, 0, 800, 600);
    	
    	//Create an instance of the JLabel object
    	JLabel userNameLabel = new JLabel("Insert your user name:");
    	//Set the userNameLabel font
    	userNameLabel.setFont(new Font("Serif", Font.PLAIN, 17));
    	
    	//Create an instance of the JLabel for indicating the user where he should type in his message
    	JLabel typeYourMessageHere = new JLabel("Type your message here:");
    	//Set the typeYourMessageHere font
    	typeYourMessageHere.setFont(new Font("Serif", Font.PLAIN, 17));
    	
    	//Create an instance of the JTextField object
    	userNameInputField = new JTextField();
    	
    	//Create instances of JButton objects
    	connectButton = new JButton("Connect");
    	disconnectButton = new JButton("Disconnect");
    	//Set the disconnectButton to disabled by default
    	disconnectButton.setEnabled(false);
    	
    	//Create an instance of the JButton for sending message
    	sendMessageButton = new JButton("Send Message");
    	//Set the sendMessageButton to disabled by default;
    	sendMessageButton.setEnabled(false);
    	//A button for allowing the user to choose whether he wants an automatic scrolling of the chatArea
    	final JButton automaticScrollingButton = new JButton("Disable automatic scrolling");
    	
    	//Create instance of JTextArea for chatArea
    	chatArea = new JTextArea();
    	//Create instance of JScrollPane for scrollChatArea, placing the chatArea inside of it
    	JScrollPane scrollChatArea = new JScrollPane(chatArea);
    	
    	//Create instance of JTextArea for the inputMessage
    	final JTextArea inputMessage = new JTextArea();
    	//Create instance of JScrollPane for scrollInputMessage, placing the inputMessage inside of it
    	JScrollPane scrollInputMessage = new JScrollPane(inputMessage);
    	
    	//Create an instance of the JLabel for indicating the user how many characters he can still type in, into the inputMessage text area
    	final JLabel charactersLeftLabel = new JLabel("800 characters left.");
    	
    	//Create an instance of the JLabel for the label of the online list
    	JLabel onlineListLabel = new JLabel("Online Users:");
    	
    	//Create an instance of the JLabel for the label of the private chat information
    	JLabel privateChatInfo = new JLabel("Double click to start private chat");
    	
    	//Create an instance of JList for the online list
    	onlineListArea = new JList<String>();
    	model = new DefaultListModel<String>();
    	onlineListArea.setModel(model);
    	//Create an instance of JScrollPane for scrollOnlineList, placing the onlineList inside of it
    	JScrollPane scrollOnlineList = new JScrollPane(onlineListArea);
    	
    	//Add an action listener for the connectButton
    	connectButton.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			System.out.println("Connect button has been pressed.");
    			
    			//If the userNameInputField is empty
    			if (userNameInputField.getText().trim().equals("")) {
    				//Information message dialog
    				JOptionPane.showMessageDialog(window, "The username can't be empty.");
    			}  else { //userNameInputField is not empty/blank
    				if (link != null) {
    					JOptionPane.showMessageDialog(window, "Dear, " + userName + " you are already connected.");
    				} else if (userNameInputField.getText().length() > 20) { 
        				JOptionPane.showMessageDialog(window, "The username can't be longer than 20 characters.");
        			} else { //The link is null and userName contains 20 or less characters
    					try {
    						//Obtain the user name
    						userName = userNameInputField.getText();
    						//Call the method to connect to a server
    						connectToServer();
    						//Set the connect button and user name input fields to disabled, since the client is connected to server
    						connectButton.setEnabled(false);
    						userNameInputField.setEnabled(false);
    						//Set the disconnect and send message buttons to enabled, since the client is connected to the server
    						disconnectButton.setEnabled(true);
    						sendMessageButton.setEnabled(true);
    					} catch (IOException e1) {
    						JOptionPane.showMessageDialog(window, "Could not establish a link with the server", "Connection error", JOptionPane.ERROR_MESSAGE);
    					}
    				}
    			}//End of else (when the user name is not empty)
    		}//End of actionPerformed
    	});//End of connectButton actionListener
    	
    	//Add an action listener for the disconnectButton
    	disconnectButton.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			//If the link is not null (i.e. the connection exists)
    			if (link != null) {
    				try {
    					//Loop through the users chatting with (i.e. private chats)
						for (int i = 0; i < usersChattingWith.size(); i++) {
							//Announce the closure of the private chat with the currently looped user, so the other user can notice it
			    			announceClosureOfPrivChat(usersChattingWith.get(i));
			    			//Call a method to close the private chat with the currently looped user chatting with
			    			closePrivChat(usersChattingWith.get(i));
						}
						//Call a method to disconnect from the server
						disconnectFromServer();
						//onlineListArea.setText("");
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(window, "Could not disconnect from the server.", "Disconnect error", JOptionPane.ERROR_MESSAGE);
					}
    			} else {
    				JOptionPane.showMessageDialog(window, "You are not connected to the server.");
    			}
    		}//End of actionPerformed
    	});//End of disconnectButton actionListener
    	
    	//Add an action listener for the sendMessageButton
    	sendMessageButton.addActionListener(new ActionListener() {
    		public void actionPerformed (ActionEvent e) {
    			
    			//If the link is up (i.e. user is connected to the chat) and the message is 800 characters or less and is not blank
    			if (link != null && inputMessage.getText().length() <= 800 && !inputMessage.getText().equals("")) {
    				
    				sendMessage(inputMessage.getText());
    				//Set the input message text area to blank
    				inputMessage.setText("");
    				//Set the charactersLeftLabel with the default number of characters that the user is allowed to type in (200 characters)
    				charactersLeftLabel.setText("800 characters left.");
    			} else if (link != null && inputMessage.getText().equals("")) { //User is connected and input message is blank
    				JOptionPane.showMessageDialog(window, "Your message can't be blank");
    			} else if (inputMessage.getText().length() > 800) {
    				JOptionPane.showMessageDialog(window, "You have gone over the limit of 800 characters per message.");
    			}
    			else { //User is not connected to the chat
    				JOptionPane.showMessageDialog(window, "You are not connected to the server.");
    			}
    		}//End of actionPerformed
    	});//End of sendMessageButton actionListener
    	
    	//Add an action listener for the automaticScrollingButton
    	automaticScrollingButton.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			//If the automatic scrolling is on, change the text of the button to reflect the change and set it to false
    			if (automaticScrolling) {
    				automaticScrollingButton.setText("Enable automatic scrolling");
    				automaticScrolling = false;
    			} else { //If the automatic scrolling is off, change the text of the button to reflect the change and set it to true
    				automaticScrollingButton.setText("Disable automatic scrolling");
    				automaticScrolling = true;
    			}
    		}//End of actionPerformed
    	});//End of action listener for automaticScrollingButton
    	
    	/**
    	 * Inner class for the EnterAction that will customise what happens when the enter button is pressed (a key listener wasn't a good approach because when pressing
    	 * enter button it would always add an extra empty line to the message, causing the message to be never empty therefore the program couldn't restrict the user
    	 * from sending empty messages when pressing enter button)
    	 * @author Adrian Fall
    	 *
    	 */
    	
    	class EnterAction extends AbstractAction
    	{
			public void actionPerformed(ActionEvent e) {
				//Automatically press the sendMessageButton for the user
				sendMessageButton.doClick();
				//Request a focus for the sendMessageButton, this is for avoiding the problem of when user is presented with the showMessageDialog
				//and he wants to press OK button via the means of pressing enter button (i.e. if this wasn't done then pressing enter to close the showMessageDialog
				//would continuously make the showMessageDialog appear, since the inputMessage is still being focused.)
				sendMessageButton.requestFocus();
			}//End of actionPerformed
    		
    	}//End class EnterAction
    	
    	//Pair the ENTER button keySTroke with the actionMapKey "enterButton"
    	inputMessage.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "enterButton");
    	//Initialise the enterAction object
    	Action enterAction = new EnterAction();
    	//Pair the actionMapKey "enterButton" with the enterAction
    	inputMessage.getActionMap().put("enterButton", enterAction);
    	
    	
    	//Add a key listener for the inputMessage text area, for counting the number of characters that the user still can type in and updating the label text
    	inputMessage.addKeyListener(new KeyAdapter() {
    		public void keyReleased(KeyEvent e) {
    			
    			//If the user releases any key, update the number of characters he can still type into the inputMessage (by the means of adding the text to a label)
    			if (inputMessage.getText().length() <= 800) {
    				int charactersLeft = 800 - inputMessage.getText().length();
    				
    				//Set the label text to indicate the user how many characters he can still type
    				charactersLeftLabel.setText(Integer.toString(charactersLeft) + " characters left.");
    			} else {
    				charactersLeftLabel.setText("You went over the limit.");
    			}
    		}//End of keyReleased method
    	});//End of key listener for inputMessage text area
    	
    	//Add a mouse listener to the onlineListArea JList
    	onlineListArea.addMouseListener(new MouseListener() {

			
			@Override
			public void mouseReleased(MouseEvent e) {
				
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				//If the left mouse button has been double-clicked or the right mouse button has been clicked
				if (e.getClickCount() == 2 || e.getButton() == 3) {
					//Obtain the selectedValue from the JList (i.e. the name of clicked user)
					String selectedValue = onlineListArea.getSelectedValue();
					
					//If the selected user is already being private chatted with
					if (usersChattingWith.contains(selectedValue)) {
						chatArea.append("You are already privately chatting with this user. \n");
						//If the user wants automatically scrolled chat area
				    	if (automaticScrolling) {
				    		//Automatically scroll the chatArea to the bottom for the user.
				        	chatArea.setCaretPosition(chatArea.getDocument().getLength());
				    	}
					} else if (userRequestedForPrivChat.contains(selectedValue)) {
						chatArea.append("Please wait for the user to accept or decline the private chat request. \n");
					}
					
					else if (!selectedValue.equals(userName)) { //If the selected user is not this user
						//Send a request for private chat to the server
						networkOutput.println("private chat");
						//Send the name of the user to be communicated with
						networkOutput.println(selectedValue);
						networkOutput.flush();
						userRequestedForPrivChat.add(selectedValue);
					} else if (selectedValue.equals(userName)) {
						chatArea.append("You can't chat with yourself, try to socialize. \n");
						//If the user wants automatically scrolled chat area
				    	if (automaticScrolling) {
				    		//Automatically scroll the chatArea to the bottom for the user.
				        	chatArea.setCaretPosition(chatArea.getDocument().getLength());
				    	}
					}
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
    		
    	});//End of mouse listener for onlineListArea
    	
    	//Add the userNameLabel to the panel
    	panel.add(userNameLabel);
    	//Position the userNameLabel
    	userNameLabel.setBounds(10, 20, 165, 20);
    	//Add the text field to the panel
    	panel.add(userNameInputField);
    	//Position the text field
    	userNameInputField.setBounds(170, 20, 200, 25);
    	
    	//Add the connectButton to the panel
    	panel.add(connectButton);
    	//Position the connectButton
    	connectButton.setBounds(375, 20, 100, 25);
    	
    	//Add the disconnectButton to the panel
    	panel.add(disconnectButton);
    	//Position the disconnectButton
    	disconnectButton.setBounds(480, 20, 100, 25);
    	
    	//Add the automaticScrollingButton to the panel
    	panel.add(automaticScrollingButton);
    	//Position the automaticScrollingButton
    	automaticScrollingButton.setBounds(378, 358, 200, 25);
    	
    	//Add chatArea to the panel
    	panel.add(scrollChatArea);
    	//Position the chatArea
    	scrollChatArea.setBounds(10, 55, 570, 300);
    	//Do not allow the chat area to be edited
    	chatArea.setEditable(false);
    	//Allow the chat area to be line wrapped (for long messages)
    	chatArea.setLineWrap(true);
    	
    	//Add the typeYourMessageHere to the panel
    	panel.add(typeYourMessageHere);
    	//Position the typeYourMessageHere
    	typeYourMessageHere.setBounds(10, 365, 225, 25);
    	
    	//Add the scrollInputMessage to the panel
    	panel.add(scrollInputMessage);
    	//Position the scrollInputMessage
    	scrollInputMessage.setBounds(10, 400, 445, 120);
    	//Allow the inputMessage to be line wrapped (for long messages)
    	
    	inputMessage.setLineWrap(true);
    	
    	
    	//Add the sendMessageButton to the panel
    	panel.add(sendMessageButton);
    	//Position the sendMessageButton
    	sendMessageButton.setBounds(460, 400, 118, 60);
    	
    	//Add the charactersLeftLabel to the panel
    	panel.add(charactersLeftLabel);
    	//Position the charactersLeftLabel
    	charactersLeftLabel.setBounds(10, 550, 150, 25);
    	
    	//Add the onlineListLabel to the panel
    	panel.add(onlineListLabel);
    	//Position the onlineListLabel
    	onlineListLabel.setBounds(650, 20, 150, 25);
    	
    	//Change the colour of privateChatInfo
    	privateChatInfo.setForeground(Color.RED);
    	//Add the privateChatInfo to the panel
    	panel.add(privateChatInfo);
    	//Position the privateChatInfo
    	privateChatInfo.setBounds(592, 400, 200, 25);
    	
    	//Add the scrollOnlineList to the panel
    	panel.add(scrollOnlineList);
    	//Position the scrollOnlineList
    	scrollOnlineList.setBounds(585, 55, 200, 350);
    	
    	//Add the panel to the window
    	window.add(panel);
    	
    	
    	return window;
    }//End of setUpWindow method
    
    /**
     * A method for connecting to the server through initialising a link Socket.
     * Additionally initialises the PrintWriter and Scanner.
     * @throws IOException - Throws an exception in case the link Socket, PrintWriter or Scanner couldn't be initialised.
     */
    private static void connectToServer() throws IOException {
    	//initialise the socket link
    	link = new Socket(host, portNumber);
    	
    	//initialise the networkOutput for sending requests to the server
    	networkOutput = new PrintWriter(link.getOutputStream(),true); 
    	
    	//initialise the network input for obtaining the reponses from the server
    	networkInput = new Scanner(link.getInputStream()); 
    	
    	//Send the user name to the server
    	networkOutput.println(userName);
    }
    
    /**
     * A method for disconnecting from the server, by closing the link Socket.
     * Additionally removes all the model elements so the online list will be blank.
     * @throws IOException - Throws an exception in case the link couldn't be closed.
     */
    private static void disconnectFromServer() throws IOException {
   
    	//Send the request we want to perform to the server
    	networkOutput.println("disconnect");
    	networkOutput.flush();
    	//Close the link and set it to null
    	link.close();
    	link = null;
    	
    	chatArea.append("You have disconnected from the chat \n");
    	
    	//If the user wants automatically scrolled chat area
    	if (automaticScrolling) {
    		//Automatically scroll the chatArea to the bottom for the user.
        	chatArea.setCaretPosition(chatArea.getDocument().getLength());
    	}
    	model.removeAllElements();
    	
    	//Enable the user name input field and connect button for the user, since he has just been disconnected.
    	userNameInputField.setEnabled(true);
    	connectButton.setEnabled(true);
    	//Disable the disconnect and sendMessage buttons, since the user has been disconnected.
    	disconnectButton.setEnabled(false);
    	sendMessageButton.setEnabled(false);
    }
    
    /**
     * A method for listening to the server responses, obtaining the server response type
     * and performing adequate action.
     * 
     * Clarification - As I have chosen to use a String for the server response types I have occurred
     * a limitation from the switch statement I wanted to use for performing the actions. The limitation
     * of the switch statement has turned out to be that it would not work with any source below the JRE 1.7, because
     * of the String compliance. Therefore even though I would like to use a switch statement for the clarity I have
     * decided that this limitation will affect many java run environments and had to use bunch of if & else if statements.
     */
    private synchronized static void listenToServer() {
    	//If the networkInput exists and it has a message
    	if (networkInput != null && networkInput.hasNext()) {
    		//Obtain the server response type
    		String serverResponse = networkInput.nextLine();
    		
    		//If the name is already used by some other client
    		if (serverResponse.equals("name already used")) {
    			try {
    				//Present the user with the message via the means of appending it to the chat area
    				chatArea.append("Somebody is already using this name, please try another one. \n");
    	    		
    	    		//If the user wants automatically scrolled chat area
    	        	if (automaticScrolling) {
    	        		//Automatically scroll the chatArea to the bottom for the user.
    	            	chatArea.setCaretPosition(chatArea.getDocument().getLength());
    	        	}
    				
    				//Close the link and set it to null
					link.close();
					link = null;
					
					//Enable the userNameInputField and connectButton, since the connection is closed.
					userNameInputField.setEnabled(true);
					connectButton.setEnabled(true);
					
					//Disable the disconnect and sendMessage buttons, since the connection is closed.
					disconnectButton.setEnabled(false);
					sendMessageButton.setEnabled(false);
				} catch (IOException e) {
					JOptionPane.showMessageDialog(window, "Could not close the link.", "Link error", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				}
    		} else if (serverResponse.equals("server shutting down")) { //If the server is shutting down
    			
    			//Call a method to close all the opened private chats
    			closeAllPrivateChats();
    			//Clear the model's elements
    			model.clear();
    			//Present the user with the message that the server is shutting down via the means of appending it to the chat area
    			chatArea.append("Server is shutting down, you are being disconnected. \n");
        		
        		//If the user wants automatically scrolled chat area
            	if (automaticScrolling) {
            		//Automatically scroll the chatArea to the bottom for the user.
                	chatArea.setCaretPosition(chatArea.getDocument().getLength());
            	}
            	
            	try {
            		//Close the link and set it to null
					link.close();
					link = null;
					
					//Enable the userNameInputField and connectButton, since the connection is closed.
					userNameInputField.setEnabled(true);
					connectButton.setEnabled(true);
					
					//Disable the disconnect and sendMessage buttons, since the connection is closed.
					disconnectButton.setEnabled(false);
					sendMessageButton.setEnabled(false);
				} catch (IOException e) {
					JOptionPane.showMessageDialog(window, "Could not close the link.", "Link error", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				}
    		} else if (serverResponse.equals("online list updated")) { //If the online list has been updated
    			
    			updateOnlineList();
    			
    		} else if (serverResponse.equals("request private chat")) { //If the response is a request for private chat
    			//Obtain the user name that has requested the private chat with this client
    			String userNameThatRequestedPrivChat = networkInput.nextLine();
    			//Call a method to show a window to this client for the requested private chat, that will allow for acceptation or declination to the private chat
    			showTheRequestForPrivChat(userNameThatRequestedPrivChat);
    		} else if (serverResponse.equals("private chat declined")) { //If the response is a declined private chat
    			String declinerName = networkInput.nextLine();
    			chatArea.append(declinerName + " has declined your request for private chat. \n");
    			userRequestedForPrivChat.remove(declinerName);
    			//If the user wants automatically scrolled chat area
            	if (automaticScrolling) {
            		//Automatically scroll the chatArea to the bottom for the user.
                	chatArea.setCaretPosition(chatArea.getDocument().getLength());
            	}
    		} else if (serverResponse.equals("start private chat")) { //If the response is to start the private chat
    			//Obtain the user name to privately chat with
    			String userToChatWith = networkInput.nextLine();
    			userRequestedForPrivChat.remove(userToChatWith);
    			startPrivateChat(userToChatWith);
    		} else if (serverResponse.equals("close private chat")) {
    			
    			//Obtain the user name that has closed the chat
    			String userThatClosedChat = networkInput.nextLine();
    			//Obtain the position in the array list of the user that closed the chat
    			int indexOfUserThatClosedChat = usersChattingWith.indexOf(userThatClosedChat);
    			
    			//Hide the window of the private chat with the user that closed the chat
    			privChatWindows.get(indexOfUserThatClosedChat).setVisible(false);
    			//Remove the name of the user that closed the private chat from the array list
    			usersChattingWith.remove(indexOfUserThatClosedChat);
    			//Remove the window of the private chat with the user that closed the chat from the array list
    			privChatWindows.remove(indexOfUserThatClosedChat);
    			//Remove the private chat area from the array list
    			privChatAreas.remove(indexOfUserThatClosedChat);
    			
    			//Present the user with a message that the private chat has been closed by the user that closed it
    			chatArea.append("Private chat has been closed by " + userThatClosedChat + "\n");
    			//If the user wants automatically scrolled chat area
            	if (automaticScrolling) {
            		//Automatically scroll the chatArea to the bottom for the user.
                	chatArea.setCaretPosition(chatArea.getDocument().getLength());
            	}
    		} else if (serverResponse.equals("private message response")) {
    			//Obtain the name of the user that sent the message
    			String userThatSentTheMessage = networkInput.nextLine();
    			//Obtain the name of the user chatting with
    			String userChattingWith = networkInput.nextLine();
    			//Obtain the private message
    			String privMessage = networkInput.nextLine();
    			//Call a method for displaying the private message
    			displayPrivateMessage(userThatSentTheMessage, userChattingWith, privMessage);
    		}
    		else  if (serverResponse.equals("chat room message response")){//If the response is a message to the chat room
    			displayChatMessage();
    		}
    	}//End of if (network input exists and contains a message)
    }//End of listenToServer method
    
    /**
     * A method for sending a message to the chat room (i.e. all online users) 
     * @param message - The message to be passed to the Server
     */
    private static void sendMessage(String message) {
    	
    	//Send the request we want to perform to the server
    	networkOutput.println("chat room message");
    	//Send the contents of the message
    	networkOutput.println(message);
    	networkOutput.flush();
    }
    /**
     * A method for updating the online list of the JList elements
     */
    private static void updateOnlineList() {
    	//Remove all elements from the JList
		model.clear();
		
		//Declare a local variable for storing the online list, obtaining an array list output from the network input
		String onlineList = networkInput.nextLine();
		//Declare a local variable for modified online list, that will replace "]" with a ", "
		modifiedOnlineList = onlineList.replaceAll("\\[", "").replaceAll("\\]", ", ");
		
		try {
			//Create an invoke and wait, placing a runnable into Event Dispatch Thread and wait until it has been executed. 
			//P.S. this has been a solution to a problem with swing threading, where randomly the online user names JList wouldn't get populated with elements.
			SwingUtilities.invokeAndWait(new Runnable() {

				@Override
				public void run() {
					//Loop the modifiedOnlineList string until it contains "\n"
					while (modifiedOnlineList.contains(",")) {
						//A local temporary variable for the first occurence of ", " in the modifiedOnlineList
						int tempFirstOccurence = modifiedOnlineList.indexOf(", ");
						//Obtain the name of the currently looped user
						String tempOnlineUserName = modifiedOnlineList.substring(0, tempFirstOccurence);
						model.addElement(tempOnlineUserName);
						
						//Substring the remaining part of the string.
						modifiedOnlineList = modifiedOnlineList.substring(tempFirstOccurence + 2);
					}
				}
			});
		} catch (InvocationTargetException e) {
			JOptionPane.showMessageDialog(window, "Error with invoking the EDT.", "Invocation EDT error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		} catch (InterruptedException e) {
			JOptionPane.showMessageDialog(window, "Error caused by interruption of the EDT.", "Interruption of EDT error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
    }//End of updateOnlineList
    
    /**
     * A method for showing the client with a request window for the private chat. 
     * The window will allow for either acceptation or declination of the private chat request.
     * @param userNameThatRequestedPrivChat - The name of the user that initially requested the private chat.
     */
    private static void showTheRequestForPrivChat(final String userNameThatRequestedPrivChat) {
    	//Create a JFrame window for showing the request of a private chat
    	final JFrame privChatRequestWindow = new JFrame("Private Chat Request");
    	
    	//Add an action listener to the window, so it allows for custom behaviour when closing the window
    	privChatRequestWindow.addWindowListener(new WindowAdapter() {
    		public void windowClosing(WindowEvent e) {
    			//Treat the window closing as declination of the request for private chat
    			declinePrivateChat(userNameThatRequestedPrivChat);
    			//Set the window to not visible
    			privChatRequestWindow.setVisible(false);
    		}//End of windowClosing method
    	});//End of action listener for window
    	
    	//Set the size of the window
    	privChatRequestWindow.setSize(500, 250);
    	//Do not allow resizing of the window
    	privChatRequestWindow.setResizable(false);
    	//Set the position of the window to be in relative to its main window
    	privChatRequestWindow.setLocationRelativeTo(window);
    			
    	//Create an instance of the JPanel object
    	JPanel panel = new JPanel();
    	//Set the panel's layout manager to null
    	panel.setLayout(null);
    	
    	//Set the bounds of the window
    	panel.setBounds(0, 0, 500, 250);
    	
    	//Create a label for showing who has requested the private chat
    	JLabel requestFromLabel = new JLabel("<html>" + userNameThatRequestedPrivChat + " has requested a private chat with you. <br> <center>Would you like to accept ?</center></html>", SwingConstants.CENTER);
    	//Create a JButton for accepting the request for private chat
    	JButton acceptButton = new JButton("Accept Request.");
    	//Create a JButton for declining the request for private chat
    	JButton declineButton = new JButton ("Decline Request.");
    	
    	//Add action listener to the accept button
    	acceptButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				acceptPrivateChat(userNameThatRequestedPrivChat);
				//Set the window to not visible
				privChatRequestWindow.setVisible(false);
			}
    		
    	});
    	
    	//Add action listener to the decline button
    	declineButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				declinePrivateChat(userNameThatRequestedPrivChat);
				//Set the window to not visible
    			privChatRequestWindow.setVisible(false);
			}
    		
    	});
    	
    	//Add the requestFromLabel to the panel
    	panel.add(requestFromLabel);
    	//Set the position of the requestFromLAbel
    	requestFromLabel.setBounds(20, 20, 480, 55);
    	
    	//Add the acceptButton to the panel
    	panel.add(acceptButton);
    	//Set the position of the acceptButton
    	acceptButton.setBounds(25, 85, 200, 50);
    	
    	//Add the declineButton to the panel
    	panel.add(declineButton);
    	//Set the position of the declineButton
    	declineButton.setBounds(235, 85, 200, 50);
    	
    	//Add the panel to the window
    	privChatRequestWindow.add(panel);
    			
        //Set the window to be visible
    	privChatRequestWindow.setVisible(true);
    }//End of showTheRequestForPrivChat method
    
    /**
     * A method for declining the private chat request.
     * @param userNameThatRequestedPrivChat - The name of the user that initially requested the private chat.
     */
    private static void declinePrivateChat(String userNameThatRequestedPrivChat) {
    	//Send a request to decline the private chat to the server
    	networkOutput.println("decline private chat");
    	//Send the name of the user name that requested the private chat, in order to decline his request
    	networkOutput.println(userNameThatRequestedPrivChat);
    	networkOutput.flush();
    }
    
    /**
     * A method for accepting the private chat request.
     * @param userNameThatRequestedPrivChat - The name of the user that initially requested the private chat.
     */
    private static void acceptPrivateChat(String userNameThatRequestedPrivChat) {
    	//Send a request to accept the private chat to the server
    	networkOutput.println("accept private chat");
    	//Send the name of the user name that requested the private chat, in order to accept his request
    	networkOutput.println(userNameThatRequestedPrivChat);
    	networkOutput.flush();
    }
    
    /**
     * A method for starting the private chat once it has been accepted.
     * The method includes a call to another method that will create the 
     * private chat window.
     * @param userToPrivChatWith - The name of the user to privately chat with.
     */
    private static void startPrivateChat(final String userToPrivChatWith) {
    	//Call a method to set up the private chat window
    	setUpPrivChatWindow(userToPrivChatWith);
    }
    
    /**
     * A method for setting up the private chat window, its layout, components,
     *  their positions and action listeners.
     * @param userChattingWith - The name of the user to privately chat with.
     */
    private static void setUpPrivChatWindow(final String userChattingWith) {
    	//Create a JFrame window for the private chat
    	final JFrame privChatWindow = new JFrame("Private Chat with " + userChattingWith);
    	
    	//Add an action listener to the window, so it allows for custom behaviour when closing the window
    	privChatWindow.addWindowListener(new WindowAdapter() {
    		public void windowClosing(WindowEvent e) {
    			//Announce the closure of the private chat, so the other user can notice it
    			announceClosureOfPrivChat(userChattingWith);
    			//Call a method to close the private chat with the user chatting with
    			closePrivChat(userChattingWith);
    			
    		}//End of windowClosing method
    	});//End of action listener for window
    	
    	//Set the size of the window
    	privChatWindow.setSize(600, 400);
    	//Do not allow resizing of the window
    	privChatWindow.setResizable(false);
    	//Set the position of the window to be in relative to its main window
    	privChatWindow.setLocationRelativeTo(window);
    			
    	//Create an instance of the JPanel object
    	JPanel panel = new JPanel();
    	//Set the panel's layout manager to null
    	panel.setLayout(null);
    	
    	//Set the bounds of the window
    	panel.setBounds(0, 0, 600, 400);
    	
    	//Create a label for showing who the user is chatting with
    	JLabel privChatLabel = new JLabel("Private chat with: " + userChattingWith, SwingConstants.CENTER);
    	//Create a text area for the private chat
    	JTextArea privChatArea = new JTextArea();
    	//Create JScrollPane for scrollPrivChatArea, placing the privChatArea inside of it
    	JScrollPane scrollPrivChatArea = new JScrollPane(privChatArea);
    	//Create instance of JTextArea for the inputMessage
    	final JTextArea inputMessage = new JTextArea();
    	//Create instance of JScrollPane for scrollInputMessage, placing the inputMessage inside of it
    	JScrollPane scrollInputMessage = new JScrollPane(inputMessage);
    	//Create a JButton for accepting the request for private chat
    	final JButton sendMessageButton = new JButton("Send Message");
    	//Create a JButton for declining the request for private chat
    	JButton closeChatButton = new JButton ("Close chat.");
    	//Create a JLabel for displaying the number of characters the user can still type in for the private message
    	final JLabel charactersLeftLabel = new JLabel("800 characters left.");
    	
    	/**
    	 * Inner class for the EnterAction that will customise what happens when the enter
    	 * button is pressed (a key listener wasn't a good approach because when pressing
    	 * enter button it would always add an extra empty line to the message, causing the
    	 * message to be never empty therefore the program couldn't restrict the user
    	 * from sending empty messages when pressing enter button)
    	 */
    	class EnterAction extends AbstractAction
    	{
			public void actionPerformed(ActionEvent e) {
				//Automatically press the sendMessageButton for the user
				sendMessageButton.doClick();
				//Request a focus for the sendMessageButton, this is for avoiding the problem of when user is presented with the showMessageDialog
				//and he wants to press OK button via the means of pressing enter button (i.e. if this wasn't done then pressing enter to close the showMessageDialog
				//would continuously make the showMessageDialog appear, since the inputMessage is still being focused.)
				sendMessageButton.requestFocus();
			}//End of actionPerformed
    		
    	}//End class EnterAction
    	
    	//Pair the ENTER button keySTroke with the actionMapKey "enterButton"
    	inputMessage.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "enterButton");
    	//Initialise the enterAction object
    	Action enterAction = new EnterAction();
    	//Pair the actionMapKey "enterButton" with the enterAction
    	inputMessage.getActionMap().put("enterButton", enterAction);
    	
    	//Add a key listener for the inputMessage text area, for counting the number of characters that the user still can type in and updating the label text
    	inputMessage.addKeyListener(new KeyAdapter() {
    		public void keyReleased(KeyEvent e) {
    			
    			//If the user releases any key, update the number of characters he can still type into the inputMessage (by the means of adding the text to a label)
    			if (inputMessage.getText().length() <= 800) {
    				int charactersLeft = 800 - inputMessage.getText().length();
    				
    				//Set the label text to indicate the user how many characters he can still type
    				charactersLeftLabel.setText(Integer.toString(charactersLeft) + " characters left.");
    			} else {
    				charactersLeftLabel.setText("You went over the limit.");
    			}
    		}//End of keyReleased method
    	});//End of key listener for inputMessage text area
    	
    	//Add an action listener for the sendMessageButton
    	sendMessageButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				
				//If the private message is more than 800 characters
				if (inputMessage.getText().length() > 800) {
					JOptionPane.showMessageDialog(privChatWindow, "You have gone over the limit of 800 characters per private message.");
				} else if (inputMessage.getText().equals("")) { //If the private message is blank
					JOptionPane.showMessageDialog(privChatWindow, "Your private message can't be blank.");
				} else  { //The private message is 800 characters or less, and is not blank
					sendPrivMessage(inputMessage.getText(), userChattingWith);
					//Set the input message text area to blank
					inputMessage.setText("");
					charactersLeftLabel.setText("800 characters left.");
				}
			}
    	});//End of action listener for sendMessageButton
    	
    	//Add an action listener for the closeChatButton
    	closeChatButton.addActionListener(new ActionListener() {
    		
    		public void actionPerformed(ActionEvent e) {
    			announceClosureOfPrivChat(userChattingWith);
    			closePrivChat(userChattingWith);
    		}
    	});//End of action listener for closeChatButton
    	
    	//Add the privChatLabel to the panel
    	panel.add(privChatLabel);
    	privChatLabel.setBounds(50, 5, 250, 25);
    	
    	//Add the scrollPrivChatArea to the panel
    	panel.add(scrollPrivChatArea);
    	//Position the scrollPrivChatArea
    	scrollPrivChatArea.setBounds(10, 35, 440, 215);
    	//Do not allow the private chat area to be edited
    	privChatArea.setEditable(false);
    	//Allow the private chat area to be line wrapped (for long messages)
    	privChatArea.setLineWrap(true);
    	
    	//Add the scrollInputMessage to the panel
    	panel.add(scrollInputMessage);
    	//Position the scrollInputMesasage
    	scrollInputMessage.setBounds(10, 260, 440, 90);
    	//Allow the inputMessage to be line wrapped (for long messages)
    	inputMessage.setLineWrap(true);
    	
    	//Add the sendMessageButton to the panel
    	panel.add(sendMessageButton);
    	//Position the sendMessageButton
    	sendMessageButton.setBounds(455, 260, 125, 90);
    	
    	//Add the closeChatButton to the panel
    	panel.add(closeChatButton);
    	//Position the closeChatButton
    	closeChatButton.setBounds(455, 5, 135, 20);
    	
    	//Add the charactersLeftLabel to the panel
    	panel.add(charactersLeftLabel);
    	//Position the charactersLeftLabel
    	charactersLeftLabel.setBounds(20, 350, 200, 20);
    	
    	//Add the panel to the window
    	privChatWindow.add(panel);
    	
    	//add the name of the user chatting with to an array list
    	usersChattingWith.add(userChattingWith);
    	//add the frame to array list
    	privChatWindows.add(privChatWindow);
    	//Add the private chat area to an array list
    	privChatAreas.add(privChatArea);
    	
    	//Set the window to be visible
    	privChatWindow.setVisible(true);
    }
    
    /**
     * A method for displaying the chat messages by obtaining it
     * from the Server's response.
     */
    //A method for displaying the chat messages
    private static void displayChatMessage() {
    	chatArea.append(networkInput.nextLine() + " \n");
		
		//If the user wants automatically scrolled chat area
    	if (automaticScrolling) {
    		//Automatically scroll the chatArea to the bottom for the user.
        	chatArea.setCaretPosition(chatArea.getDocument().getLength());
    	}
    }
    
    /**
     * A method for announcing that the client is closing the private chat with a
     * specific person/user, through sending a request to the Server with his/her name. 
     * @param userToAnnounce - The name of the user to announce the closure of private chat.
     */
    //A method for announcing that the client is closing the private chat
    private static void announceClosureOfPrivChat(String userToAnnounce) {
    	//Send the request to the server that the client is announcing a closure of the private chat
    	networkOutput.println("announce closure private chat");
    	//Send the name of the user to announce about the closure of chat (i.e. the other client that's being part of the private chat)
    	networkOutput.println(userToAnnounce);
    	
    	networkOutput.flush();
    }
    /**
     * A method for closing the private chat with specific user that this client
     * is chatting with.
     * @param userChattingWith - The user that this client is privately chatting with.
     */
    private static void closePrivChat(String userChattingWith) {
    
    	//Obtain the index of the userChattingWith from the usersChattingWith array list
    	int indexOfUserChattingWith = usersChattingWith.indexOf(userChattingWith);
	
    	//Remove the userChattingWith from the usersChattingWith array list, based on the index obtained above
    	usersChattingWith.remove(indexOfUserChattingWith);
	
    	//Set the window of the private chat with userChattingWith, based on the index obtained above
    	privChatWindows.get(indexOfUserChattingWith).setVisible(false);
    	//Remove the window of the private chat with userChattingWith, based on the index obtained above
    	privChatWindows.remove(indexOfUserChattingWith);
    	//Remove the private chat area from the array list
    	privChatAreas.remove(indexOfUserChattingWith);
    }//End of closePrivChat method
    
    /**
     * A method for sending a private message to a specific user that
     * this client is chatting with.
     * @param privMessage - The private message to be sent.
     * @param userChattingWith - The user that this client is privately chatting with.
     */
    private static void sendPrivMessage(String privMessage, String userChattingWith) {
    	//Send the request we want to perform to the server
    	networkOutput.println("private message");
    	
    	//Send the name of the user chatting with
    	networkOutput.println(userChattingWith);
    	
    	//Send the contents of the message
    	networkOutput.println(privMessage);
    	networkOutput.flush();
    }//End of sendPrivMessage method
    
    /**
     * A method for displaying the private message on this
     * client's private chat.
     * @param userThatSentTheMessage - The name of the user who sent the message
     * @param userChattingWith - The name of the user that this client is chatting with.
     * @param privMessage - The private message.
     */
    private static void displayPrivateMessage(String userThatSentTheMessage, String userChattingWith, String privMessage) {
    	//Obtain the index of the userChattingWith from the usersChattingWith array list
    	int indexOfUserChattingWith = usersChattingWith.indexOf(userChattingWith);
    	
    	//If the user that sent the message is this client
    	if (userThatSentTheMessage.equals(userName)) {
    		//Append the chat area of the window with this client's message
    		privChatAreas.get(indexOfUserChattingWith).append("You say: " + privMessage + "\n");
    	} else { //If the user that sent the message isn't this client
    		//Append the chat area of the window, with the userChattingWith
        	privChatAreas.get(indexOfUserChattingWith).append(userChattingWith + " says: " +privMessage + "\n");
    	}
    }//End of displayPrivateMessage method
    
    /**
     * A method for closing all the active private chat windows.
     */
    private static void closeAllPrivateChats() {
    	//Loop through every user chatting with array list
    	for (int i = 0; i < usersChattingWith.size(); i++) {
    		//Set the currently looped private chat window to not visible
    		privChatWindows.get(i).setVisible(false);
    		//Remove the currently looped user chatting with from the array list
    		usersChattingWith.remove(i);
    		//Remove the currently looped user chatting with chat area from the array list
    		privChatAreas.remove(i);
    		//Remove the currently looped user chatting with window from the array list
    		privChatWindows.remove(i);
    		
    		//Decrement the i, since we have just removed an entry from the usersChattingWith array list
    		i--;
    	}
    }//End of closeAllPrivateChats method
}