import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.io.*;

public class TreeDiameterSwing extends JFrame {

    static class Vertex {
        int id;
        int x, y;
        Color color;
        List<Vertex> neighbors;

        Vertex(int id) {
            this.id = id;
            this.color = Color.BLUE;
            this.neighbors = new ArrayList<>();
        }
    }

    static class Edge {
        Vertex u, v;
        Color color;

        Edge(Vertex u, Vertex v) {
            this.u = u;
            this.v = v;
            color = Color.BLACK;
        }
    }

    private final List<Vertex> vertices = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();
    private List<Vertex> diameterPath = new ArrayList<>();
    private int diameterLength = 0;

    private DrawingPanel drawingPanel;
    private JLabel infoLabel;

    public TreeDiameterSwing() {
        setTitle("Поиск диаметра дерева");
        setSize(1100, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        createSampleTree();
        findDiameter();
        setupUI();
    }

    private void createSampleTree() {
        for (int i = 0; i < 10; i++) {
            vertices.add(new Vertex(i));
        }

        for (int i = 0; i < vertices.size(); i++) {
            double angle = 2 * Math.PI * i / vertices.size();
            vertices.get(i).x = 400 + (int)(240 * Math.cos(angle));
            vertices.get(i).y = 300 + (int)(240 * Math.sin(angle));
        }

        addEdge(0, 1);
        addEdge(1, 2);
        addEdge(1, 3);
        addEdge(2, 4);
        addEdge(2, 5);
        addEdge(3, 6);
        addEdge(6, 7);
        addEdge(7, 8);
        addEdge(8, 9);
    }

    private void addEdge(int uId, int vId) {
        if (uId >= vertices.size() || vId >= vertices.size()) return;

        Vertex u = vertices.get(uId);
        Vertex v = vertices.get(vId);

        for (Edge edge : edges) {
            if ((edge.u == u && edge.v == v) || (edge.u == v && edge.v == u)) return;
        }

        u.neighbors.add(v);
        v.neighbors.add(u);
        edges.add(new Edge(u, v));
    }

    private void loadFromEdgeList(String filename) {
        vertices.clear();
        edges.clear();
        diameterPath.clear();
        diameterLength = 0;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            int vertexCount = Integer.parseInt(reader.readLine().trim());

            for (int i = 0; i < vertexCount; i++) {
                vertices.add(new Vertex(i));
            }

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.trim().split("\\s+");
                if (parts.length >= 2) {
                    int u = Integer.parseInt(parts[0]);
                    int v = Integer.parseInt(parts[1]);
                    addEdge(u, v);
                }
            }
            reader.close();

            setPositions();
        } catch (Exception e) {
            createSampleTree();
        }
    }

    private void loadFromAdjacencyMatrix(String filename) {
        vertices.clear();
        edges.clear();
        diameterPath.clear();
        diameterLength = 0;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            int vertexCount = Integer.parseInt(reader.readLine().trim());

            for (int i = 0; i < vertexCount; i++) {
                vertices.add(new Vertex(i));
            }

            for (int i = 0; i < vertexCount; i++) {
                String[] parts = reader.readLine().trim().split("\\s+");
                for (int j = i + 1; j < vertexCount; j++) {
                    if (Integer.parseInt(parts[j]) == 1) {
                        addEdge(i, j);
                    }
                }
            }
            reader.close();

            setPositions();
        } catch (Exception e) {
            createSampleTree();
        }
    }

    private void setPositions() {
        for (int i = 0; i < vertices.size(); i++) {
            double angle = 2 * Math.PI * i / vertices.size();
            vertices.get(i).x = 400 + (int)(Math.min(200, vertices.size() * 15) * Math.cos(angle));
            vertices.get(i).y = 300 + (int)(Math.min(200, vertices.size() * 15) * Math.sin(angle));
        }
    }

    private void findDiameter() {
        if (vertices.isEmpty()) return;

        DFSResult r1 = dfs(vertices.get(0), null);
        Vertex v1 = r1.farthest;
        DFSResult r2 = dfs(v1, null);
        Vertex v2 = r2.farthest;
        diameterPath = findPath(v1, v2);
        diameterLength = diameterPath.size() - 1;
    }

    static class DFSResult {
        Vertex farthest;
        int distance;
        DFSResult(Vertex v, int d) {
            farthest = v;
            distance = d;
        }
    }

    private DFSResult dfs(Vertex current, Vertex parent) {
        Vertex farthest = current;
        int maxDist = 0;

        for (Vertex n : current.neighbors) {
            if (n != parent) {
                DFSResult r = dfs(n, current);
                int d = r.distance + 1;
                if (d > maxDist) {
                    maxDist = d;
                    farthest = r.farthest;
                }
            }
        }
        return new DFSResult(farthest, maxDist);
    }

    private List<Vertex> findPath(Vertex start, Vertex end) {
        Map<Vertex, Vertex> parent = new HashMap<>();
        Queue<Vertex> queue = new LinkedList<>();
        Set<Vertex> visited = new HashSet<>();

        queue.add(start);
        visited.add(start);
        parent.put(start, null);

        while (!queue.isEmpty()) {
            Vertex v = queue.poll();
            if (v == end) break;
            for (Vertex n : v.neighbors) {
                if (!visited.contains(n)) {
                    visited.add(n);
                    parent.put(n, v);
                    queue.add(n);
                }
            }
        }

        List<Vertex> path = new ArrayList<>();
        Vertex v = end;
        while (v != null) {
            path.add(0, v);
            v = parent.get(v);
        }
        return path;
    }

    private void setupUI() {
        drawingPanel = new DrawingPanel();

        JPanel buttons = new JPanel();
        JButton showBtn = new JButton("Показать диаметр");
        JButton resetBtn = new JButton("Сбросить отображение диаметра");
        JButton randomBtn = new JButton("Создать случайное дерево");
        JButton edgesBtn = new JButton("Загрузить список рёбер");
        JButton matrixBtn = new JButton("Загрузить матрицу смежности");

        showBtn.addActionListener(e -> showPath());
        resetBtn.addActionListener(e -> reset());
        randomBtn.addActionListener(e -> randomTree());
        edgesBtn.addActionListener(e -> {
            loadFromEdgeList("tree_edges.txt");
            findDiameter();
            drawingPanel.repaint();
            updateInfo();
        });
        matrixBtn.addActionListener(e -> {
            loadFromAdjacencyMatrix("tree_adjacency.txt");
            findDiameter();
            drawingPanel.repaint();
            updateInfo();
        });

        buttons.add(showBtn);
        buttons.add(resetBtn);
        buttons.add(randomBtn);
        buttons.add(edgesBtn);
        buttons.add(matrixBtn);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        infoLabel = new JLabel("<html><font size='5'>Вершин: " + vertices.size() +
                               "<br>Диаметр: " + diameterLength + "</font></html>");
        infoPanel.add(infoLabel);

        add(drawingPanel, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        add(infoPanel, BorderLayout.EAST);
    }

    private void showPath() {
        for (Vertex v : vertices) v.color = Color.BLUE;
        for (Edge e : edges) e.color = Color.BLACK;

        for (int i = 0; i < diameterPath.size(); i++) {
            Vertex v = diameterPath.get(i);
            v.color = (i == 0 || i == diameterPath.size() - 1) ? Color.GREEN : Color.YELLOW;
        }

        for (int i = 0; i < diameterPath.size() - 1; i++) {
            Vertex u = diameterPath.get(i);
            Vertex v = diameterPath.get(i + 1);
            for (Edge e : edges) {
                if ((e.u == u && e.v == v) || (e.u == v && e.v == u)) {
                    e.color = Color.RED;
                    break;
                }
            }
        }

        drawingPanel.repaint();
        updateInfo();
    }

    private void reset() {
        for (Vertex v : vertices) v.color = Color.BLUE;
        for (Edge e : edges) e.color = Color.BLACK;
        drawingPanel.repaint();
    }

    private void randomTree() {
        Random rand = new Random();
        int n = 6 + rand.nextInt(6);

        vertices.clear();
        edges.clear();
        diameterPath.clear();
        diameterLength = 0;

        for (int i = 0; i < n; i++) {
            vertices.add(new Vertex(i));
        }

        List<Integer> connected = new ArrayList<>();
        connected.add(0);

        for (int i = 1; i < n; i++) {
            int connectTo = connected.get(rand.nextInt(connected.size()));
            addEdge(connectTo, i);
            connected.add(i);
        }

        setPositions();
        findDiameter();
        drawingPanel.repaint();
        updateInfo();
    }

    private void updateInfo() {
        StringBuilder pathStr = new StringBuilder();
        for (Vertex v : diameterPath) {
            pathStr.append(v.id).append(" ");
        }
        infoLabel.setText("<html><font size='5'>Вершин: " + vertices.size() +
                          "<br>Диаметр: " + diameterLength +
                          "<br>Путь: " + pathStr + "</font></html>");    }

    class DrawingPanel extends JPanel {
        private static final int RADIUS = 25;

        public DrawingPanel() {
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            for (Edge edge : edges) {
                g2d.setColor(edge.color);
                g2d.setStroke(new BasicStroke(edge.color == Color.RED ? 5 : 2));
                g2d.drawLine(edge.u.x, edge.u.y, edge.v.x, edge.v.y);
            }

            for (Vertex v : vertices) {
                g2d.setColor(v.color);
                g2d.fillOval(v.x - RADIUS, v.y - RADIUS, RADIUS * 2, RADIUS * 2);
                g2d.setColor(Color.BLACK);
                g2d.drawOval(v.x - RADIUS, v.y - RADIUS, RADIUS * 2, RADIUS * 2);
                g2d.drawString(String.valueOf(v.id), v.x - 5, v.y + 5);
            }
        }
    }

    public static void main(String[] args) {
        TreeDiameterSwing app = new TreeDiameterSwing();
        app.setVisible(true);
    }
}