package filters;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.util.leap.Serializable;
import jade.core.behaviours.CyclicBehaviour;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class AgentFilter2 extends Agent {
    private JFrame frame;
    private JLabel filteredImageLabel;

    protected void setup() {
        // Initialize GUI components
        frame = new JFrame("Agent Filter Grayscale");
        filteredImageLabel = new JLabel();

        // Add components to the frame
        frame.add(filteredImageLabel);

        // Set frame properties
        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(Color.lightGray);  // Set background color
        frame.setLayout(new FlowLayout());
        frame.setVisible(true);

        // Register the message handler
        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    try {
                        byte[] imageData = (byte[]) msg.getContentObject();
                        BufferedImage receivedImage = ImageIO.read(new ByteArrayInputStream(imageData));

                        // Apply Grayscale filter
                        BufferedImage filteredImage = applyGrayscaleFilter(receivedImage);

                        // Save the filtered image to a file and get the file path
                        String filePath = saveFilteredImage(filteredImage, "Grayscale");

                        // Send the file path to the master agent
                        sendFilePathToMaster(filePath, "AgentFilter2");

                        // Display the centered and resized image in AgentFilter2's GUI
                        displayFilteredImage(filteredImage);
                    } catch (UnreadableException | IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    block();
                }
            }
        });
    }

    private BufferedImage applyGrayscaleFilter(BufferedImage image) {
        // Convert the image to grayscale
        BufferedImage grayscaleImage = new BufferedImage(
                image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

        Graphics g = grayscaleImage.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();

        return grayscaleImage;
    }

    private String saveFilteredImage(BufferedImage filteredImage, String filterName) {
        try {
            // Create a unique file name based on timestamp
            String fileName = filterName + "_" + System.currentTimeMillis() + ".jpg";
            File outputFile = new File(fileName);

            // Save the filtered image to the file
            ImageIO.write(filteredImage, "jpg", outputFile);

            // Return the file path
            return outputFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void sendFilePathToMaster(String filePath, String agentName) {
        // Send the file path to the master agent
        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        message.addReceiver(new AID("MasterAgent", AID.ISLOCALNAME));
        try {
            // Send the file path and agent name as content
            message.setContentObject(new FilteredImageInfo(filePath, agentName));
            send(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void displayFilteredImage(BufferedImage filteredImage) {
        // Resize the image to fit the frame size
        int width = frame.getWidth();
        int height = frame.getHeight();
        Image resizedImage = filteredImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);

        // Center the image
        int x = (width - resizedImage.getWidth(null)) / 2;
        int y = (height - resizedImage.getHeight(null)) / 2;

        // Display the centered and resized image in AgentFilter2's GUI
        ImageIcon icon = new ImageIcon(resizedImage);
        filteredImageLabel.setIcon(icon);
        frame.revalidate();
        frame.repaint();
    }

    public static class FilteredImageInfo implements Serializable {
        private String filePath;
        private String agentName;

        public FilteredImageInfo(String filePath, String agentName) {
            this.filePath = filePath;
            this.agentName = agentName;
        }

        public String getFilePath() {
            return filePath;
        }

        public String getAgentName() {
            return agentName;
        }
    }
}
