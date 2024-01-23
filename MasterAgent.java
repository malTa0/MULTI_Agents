package filters;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.util.leap.Serializable;
import jade.core.behaviours.CyclicBehaviour;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MasterAgent extends Agent {
    private JFrame frame;
    private JLabel imageLabel;
    private Map<String, String> filteredImagesInfo = new HashMap<>();

    protected void setup() {
        // Initialize GUI components
        frame = new JFrame("Image Processing in Multi-Agent System");
        JPanel panel = new JPanel(new BorderLayout());
        JButton uploadButton = new JButton("Upload Image");
        JButton sendButton = new JButton("Send Image");
        imageLabel = new JLabel();

        // Customize UI with colors
        panel.setBackground(new Color(75, 0, 130)); // Dark Purple
        uploadButton.setBackground(new Color(123, 104, 238)); // MediumSlateBlue
        uploadButton.setForeground(Color.white);
        sendButton.setBackground(new Color(173, 216, 230)); // LightSkyBlue
        sendButton.setForeground(Color.darkGray);

        // Add action listeners to the buttons
        uploadButton.addActionListener(e -> handleImageUpload());
        sendButton.addActionListener(e -> sendImageToFilterAgents());

        // Add components to the panel and frame
        panel.add(uploadButton, BorderLayout.WEST);
        panel.add(sendButton, BorderLayout.EAST);
        frame.add(panel, BorderLayout.NORTH);
        frame.add(new JScrollPane(imageLabel), BorderLayout.CENTER);

        // Set frame properties
        frame.setSize(1200, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        // Register the message handler
        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    handleReceivedMessage(msg);
                } else {
                    block();
                }
            }
        });
    }

    private void handleImageUpload() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            try {
                BufferedImage uploadedImage = ImageIO.read(selectedFile);
                displayUploadedImage(uploadedImage);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void sendImageToFilterAgents() {
        BufferedImage uploadedImage = getImageFromLabel();
        sendImageToAgentFilterSobel(uploadedImage);
        sendImageToAgentFilterPrewitt(uploadedImage);
        sendImageToAgentFilterGaussian(uploadedImage);
    }

    private void displayUploadedImage(BufferedImage image) {
        ImageIcon icon = new ImageIcon(image);
        imageLabel.setIcon(icon);
        frame.revalidate();
        frame.repaint();
    }

    private BufferedImage getImageFromLabel() {
        ImageIcon icon = (ImageIcon) imageLabel.getIcon();
        if (icon != null) {
            // Get the image from the icon
            Image image = icon.getImage();

            // Convert to BufferedImage if not already
            if (image instanceof BufferedImage) {
                return (BufferedImage) image;
            }

            // Create a BufferedImage and draw the image onto it
            BufferedImage bufferedImage = new BufferedImage(
                    image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics g = bufferedImage.createGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();

            return bufferedImage;
        }
        return null;
    }

    private void sendImageToAgentFilterSobel(BufferedImage image) {
        sendImageToAgentFilter("AgentFilter1", image);
    }

    private void sendImageToAgentFilterPrewitt(BufferedImage image) {
        sendImageToAgentFilter("AgentFilter2", image);
    }

    private void sendImageToAgentFilterGaussian(BufferedImage image) {
        sendImageToAgentFilter("AgentFilter3", image);
    }

    private void sendImageToAgentFilter(String agentName, BufferedImage image) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", baos);
            byte[] imageData = baos.toByteArray();

            ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
            message.addReceiver(new AID(agentName, AID.ISLOCALNAME));
            message.setContentObject(imageData);

            send(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleReceivedMessage(ACLMessage msg) {
        try {
            String senderLocalName = msg.getSender().getLocalName();
            java.io.Serializable contentObject = msg.getContentObject();

            if (senderLocalName.equals("AgentFilter1") && contentObject instanceof AgentFilter1.FilteredImageInfo) {
                AgentFilter1.FilteredImageInfo filteredImageInfo = (AgentFilter1.FilteredImageInfo) contentObject;
                filteredImagesInfo.put(filteredImageInfo.getAgentName(), filteredImageInfo.getFilePath());
            } else if (senderLocalName.equals("AgentFilter2") && contentObject instanceof AgentFilter2.FilteredImageInfo) {
                AgentFilter2.FilteredImageInfo filteredImageInfo = (AgentFilter2.FilteredImageInfo) contentObject;
                filteredImagesInfo.put(filteredImageInfo.getAgentName(), filteredImageInfo.getFilePath());
            } else if (senderLocalName.equals("AgentFilter3") && contentObject instanceof AgentFilter3.FilteredImageInfo) {
                AgentFilter3.FilteredImageInfo filteredImageInfo = (AgentFilter3.FilteredImageInfo) contentObject;
                filteredImagesInfo.put(filteredImageInfo.getAgentName(), filteredImageInfo.getFilePath());
            }

            displayFilteredImages();
        } catch (UnreadableException e) {
            e.printStackTrace();
        }
    }

    private void displayFilteredImages() {
        JFrame filteredImagesFrame = new JFrame("Filtered Images");
        // Use FlowLayout to display images horizontally
        JPanel imagesPanel = new JPanel(new FlowLayout());
        imagesPanel.setBackground(new Color(211, 211, 211)); // LightGray

        // Display the original image first
        try {
            BufferedImage originalImage = getImageFromLabel();
            int width = 200;
            int height = 150;
            Image resizedOriginalImage = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            ImageIcon originalIcon = new ImageIcon(resizedOriginalImage);

            JPanel originalPanel = new JPanel(new BorderLayout());
            JLabel originalLabel = new JLabel(originalIcon);
            originalPanel.add(originalLabel, BorderLayout.CENTER);
            originalPanel.setBorder(BorderFactory.createEtchedBorder());
            originalPanel.setBackground(new Color(211, 211, 211)); // LightGray

            imagesPanel.add(originalPanel);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Display the filtered images
        for (Map.Entry<String, String> entry : filteredImagesInfo.entrySet()) {
            String agentName = entry.getKey();
            String filePath = entry.getValue();

            try {
                BufferedImage filteredImage = ImageIO.read(new File(filePath));

                // Resize the image to fit the frame size
                int width = 200;
                int height = 150;
                Image resizedImage = filteredImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);

                ImageIcon icon = new ImageIcon(resizedImage);

                JPanel imagePanel = new JPanel(new BorderLayout());
                JLabel imageLabel = new JLabel(icon);

                // Set the caption text with the name of the filter
                JLabel captionLabel = new JLabel(getFilterName(agentName));

                // Add a button to show the description
                JButton descriptionButton = new JButton("Show Description");
                descriptionButton.addActionListener(e -> showDescription(agentName));

                imagePanel.add(imageLabel, BorderLayout.CENTER);
                imagePanel.add(captionLabel, BorderLayout.NORTH);
                imagePanel.add(descriptionButton, BorderLayout.SOUTH);
                imagePanel.setBorder(BorderFactory.createEtchedBorder());
                imagePanel.setBackground(new Color(211, 211, 211)); // LightGray

                imagesPanel.add(imagePanel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Add the panel to the frame
        filteredImagesFrame.add(imagesPanel);

        // Add a big title at the top
        JLabel titleLabel = new JLabel("Filtered Images from Agents");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        filteredImagesFrame.add(titleLabel, BorderLayout.NORTH);

        // Adjust the frame size to fit the content
        filteredImagesFrame.pack();
        filteredImagesFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        filteredImagesFrame.setLocationRelativeTo(null); // Center the frame
        filteredImagesFrame.setVisible(true);
    }

    private String getFilterName(String agentName) {
        switch (agentName) {
            case "AgentFilter1":
                return "Sobel Filter";
            case "AgentFilter2":
                return "Prewitt Filter";
            case "AgentFilter3":
                return "Gaussian Filter";
            default:
                return "Unknown Filter";
        }
    }

    private String getFilterDescription(String agentName) {
        switch (agentName) {
            case "AgentFilter1":
                return "Sobel Filter - Detects edges in the image using Sobel operator.";
            case "AgentFilter2":
                return "Prewitt Filter - Applies Prewitt operator to detect edges in the image.";
            case "AgentFilter3":
                return "Gaussian Filter - Applies a Gaussian blur to the image.";
            default:
                return "Unknown Filter";
        }
    }

    private void showDescription(String agentName) {
        String description = getFilterDescription(agentName);
        JOptionPane.showMessageDialog(null, description, "Filter Description", JOptionPane.INFORMATION_MESSAGE);
    }
}
