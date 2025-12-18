import javax.swing.*;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.io.*;

public class TreeDiameterSwing extends JFrame {

    static class Vertex {
        int id;
        int x, y;
        Color color;
        List<Edge> edges;
        Map<Vertex, Integer> weights;

        Vertex(int id) {
            this.id = id;
            this.color = Color.BLUE;
            this.edges = new ArrayList<>();
            this.weights = new HashMap<>();
        }
    }

    static class Edge {
        Vertex u, v;
        Color color;
        int weight;
        String weightLabel;

        Edge(Vertex u, Vertex v, int weight) {
            this.u = u;
            this.v = v;
            this.weight = weight;
            this.color = Color.BLACK;
            this.weightLabel = String.valueOf(weight);
        }
    }

    static class DFSResult {
        Vertex farthest;
        int distance;
        int totalWeight;

        DFSResult(Vertex v, int d, int w) {
            farthest = v;
            distance = d;
            totalWeight = w;
        }
    }

    private final List<Vertex> vertices = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();
    private List<Vertex> diameterPath = new ArrayList<>();
    private int diameterLength = 0;
    private int diameterWeight = 0;

    private DrawingPanel drawingPanel;
    private JLabel infoLabel;

    public TreeDiameterSwing() {
        setTitle("Поиск диаметра дерева (взвешенные графы)");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setupUI();
        createSampleTree();
        findDiameter();
        updateInfo();
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

        addEdge(0, 1, 1);
        addEdge(1, 2, 1);
        addEdge(1, 3, 1);
        addEdge(2, 4, 1);
        addEdge(2, 5, 1);
        addEdge(3, 6, 1);
        addEdge(6, 7, 1);
        addEdge(7, 8, 1);
        addEdge(8, 9, 1);

        System.out.println("Создано дерево: " + vertices.size() + " вершин, " + edges.size() + " рёбер");
    }

    private void addEdge(int uId, int vId, int weight) {
        if (uId >= vertices.size() || vId >= vertices.size()) return;

        Vertex u = vertices.get(uId);
        Vertex v = vertices.get(vId);

        for (Edge edge : edges) {
            if ((edge.u == u && edge.v == v) || (edge.u == v && edge.v == u)) {
                return;
            }
        }

        Edge edge = new Edge(u, v, weight);
        edges.add(edge);
        u.edges.add(edge);
        v.edges.add(edge);
        u.weights.put(v, weight);
        v.weights.put(u, weight);
    }

    private void addEdge(int uId, int vId) {
        addEdge(uId, vId, 1);
    }

    private boolean isTree() {
        if (vertices.isEmpty()) return false;

        int edgeCount = edges.size();
        int vertexCount = vertices.size();

        if (edgeCount != vertexCount - 1) {
            System.out.println("Ошибка: количество рёбер (" + edgeCount + ") != количество вершин-1 (" + (vertexCount-1) + ")");
            return false;
        }

        Set<Vertex> visited = new HashSet<>();
        if (hasCycle(vertices.get(0), null, visited)) {
            System.out.println("Ошибка: граф содержит циклы");
            return false;
        }

        boolean isConnected = visited.size() == vertexCount;
        if (!isConnected) {
            System.out.println("Ошибка: граф не связный, посещено " + visited.size() + " из " + vertexCount + " вершин");
        }

        return isConnected;
    }

    private boolean hasCycle(Vertex current, Vertex parent, Set<Vertex> visited) {
        visited.add(current);

        for (Edge edge : current.edges) {
            Vertex neighbor = (edge.u == current) ? edge.v : edge.u;
            if (!visited.contains(neighbor)) {
                if (hasCycle(neighbor, current, visited)) {
                    return true;
                }
            } else if (!neighbor.equals(parent)) {
                return true;
            }
        }

        return false;
    }

    private void loadFromEdgeList(String filename) {
        System.out.println("Загрузка дерева из списка рёбер: " + filename);

        vertices.clear();
        edges.clear();
        diameterPath.clear();
        diameterLength = 0;
        diameterWeight = 0;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            int vertexCount = Integer.parseInt(reader.readLine().trim());
            System.out.println("Количество вершин в файле: " + vertexCount);

            for (int i = 0; i < vertexCount; i++) {
                vertices.add(new Vertex(i));
            }

            int edgeCount = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.trim().split("\\s+");
                if (parts.length >= 2) {
                    int u = Integer.parseInt(parts[0]);
                    int v = Integer.parseInt(parts[1]);
                    int weight = (parts.length >= 3) ? Integer.parseInt(parts[2]) : 1;
                    addEdge(u, v, weight);
                    edgeCount++;
                }
            }
            reader.close();

            System.out.println("Загружено " + edgeCount + " рёбер");

            if (!isTree()) {
                System.out.println("Загруженный граф не является деревом!");
                vertices.clear();
                edges.clear();
                diameterPath.clear();
                diameterLength = 0;
                diameterWeight = 0;
                createSampleTree();
                setPositions();
                findDiameter();
                drawingPanel.repaint();
                updateInfo();
                return;
            }

            setPositions();
            findDiameter();
            drawingPanel.repaint();
            updateInfo();
            System.out.println("Дерево успешно загружено");

        } catch (Exception e) {
            System.out.println("Ошибка при чтении файла: " + e.getMessage());
            vertices.clear();
            edges.clear();
            diameterPath.clear();
            diameterLength = 0;
            diameterWeight = 0;
            createSampleTree();
            setPositions();
            findDiameter();
            drawingPanel.repaint();
            updateInfo();
        }
    }

    private void loadFromAdjacencyMatrix(String filename) {
        System.out.println("Загрузка дерева из матрицы смежности: " + filename);

        vertices.clear();
        edges.clear();
        diameterPath.clear();
        diameterLength = 0;
        diameterWeight = 0;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            int vertexCount = Integer.parseInt(reader.readLine().trim());
            System.out.println("Количество вершин в файле: " + vertexCount);

            for (int i = 0; i < vertexCount; i++) {
                vertices.add(new Vertex(i));
            }

            int edgeCount = 0;
            for (int i = 0; i < vertexCount; i++) {
                String[] parts = reader.readLine().trim().split("\\s+");
                for (int j = i + 1; j < vertexCount; j++) {
                    int weight = Integer.parseInt(parts[j]);
                    if (weight > 0) {
                        addEdge(i, j, weight);
                        edgeCount++;
                    }
                }
            }
            reader.close();

            System.out.println("Загружено " + edgeCount + " рёбер");

            if (!isTree()) {
                System.out.println("Загруженный граф не является деревом!");
                vertices.clear();
                edges.clear();
                diameterPath.clear();
                diameterLength = 0;
                diameterWeight = 0;
                createSampleTree();
                setPositions();
                findDiameter();
                drawingPanel.repaint();
                updateInfo();
                return;
            }

            setPositions();
            findDiameter();
            drawingPanel.repaint();
            updateInfo();
            System.out.println("Дерево успешно загружено");

        } catch (Exception e) {
            System.out.println("Ошибка при чтении файла: " + e.getMessage());
            vertices.clear();
            edges.clear();
            diameterPath.clear();
            diameterLength = 0;
            diameterWeight = 0;
            createSampleTree();
            setPositions();
            findDiameter();
            drawingPanel.repaint();
            updateInfo();
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

        diameterPath = findWeightedPath(v1, v2);
        diameterWeight = calculatePathWeight(diameterPath);
        diameterLength = diameterPath.size() - 1;

        System.out.println("Диаметр найден:");
        System.out.println("• Длина (количество рёбер): " + diameterLength);
        System.out.println("• Вес: " + diameterWeight);
        System.out.print("• Путь: ");
        for (Vertex v : diameterPath) {
            System.out.print(v.id + " ");
        }
        System.out.println();
    }

    private DFSResult dfs(Vertex current, Vertex parent) {
        Vertex farthest = current;
        int maxDist = 0;
        int maxWeight = 0;

        for (Edge edge : current.edges) {
            Vertex neighbor = (edge.u == current) ? edge.v : edge.u;
            if (neighbor != parent) {
                DFSResult r = dfs(neighbor, current);
                int d = r.distance + 1;
                int w = r.totalWeight + edge.weight;
                if (d > maxDist) {
                    maxDist = d;
                    maxWeight = w;
                    farthest = r.farthest;
                }
            }
        }
        return new DFSResult(farthest, maxDist, maxWeight);
    }

    private List<Vertex> findWeightedPath(Vertex start, Vertex end) {
        if (start == null || end == null || vertices.isEmpty()) {
            return new ArrayList<>();
        }

        if (start == end) {
            return Collections.singletonList(start);
        }

        Map<Vertex, Vertex> parent = new HashMap<>();
        Stack<Vertex> stack = new Stack<>();
        Set<Vertex> visited = new HashSet<>();

        stack.push(start);
        parent.put(start, null);
        visited.add(start);

        // DFS
        while (!stack.isEmpty()) {
            Vertex current = stack.pop();

            if (current == end) {
                break;
            }

            for (Edge edge : current.edges) {
                Vertex neighbor = (edge.u == current) ? edge.v : edge.u;
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    parent.put(neighbor, current);
                    stack.push(neighbor);
                }
            }
        }

        List<Vertex> path = new ArrayList<>();
        for (Vertex v = end; v != null; v = parent.get(v)) {
            path.add(v);
        }

        Collections.reverse(path);

        if (path.isEmpty() || path.get(0) != start) {
            System.out.println("Путь не найден: " + start.id + " -> " + end.id);
            return new ArrayList<>();
        }

        return path;
    }

    private int calculatePathWeight(List<Vertex> path) {
        if (path == null || path.size() < 2) return 0;

        int totalWeight = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            Vertex u = path.get(i);
            Vertex v = path.get(i + 1);
            Integer weight = u.weights.get(v);
            if (weight != null) {
                totalWeight += weight;
            }
        }
        return totalWeight;
    }

    private String getFilePath(String filename) {
        String classLocation = TreeDiameterSwing.class.getProtectionDomain()
                .getCodeSource().getLocation().getPath();

        classLocation = java.net.URLDecoder.decode(classLocation, StandardCharsets.UTF_8);

        File classFile = new File(classLocation);

        if (classFile.isFile() && classLocation.endsWith(".jar")) {
            classFile = classFile.getParentFile();
        }

        File fileNearClass = new File(classFile, filename);
        if (fileNearClass.exists()) {
            return fileNearClass.getAbsolutePath();
        }

        File parentDir = classFile.getParentFile();
        if (parentDir != null) {
            File inputDir = new File(parentDir, "input");
            File fileInInput = new File(inputDir, filename);
            if (fileInInput.exists()) {
                return fileInInput.getAbsolutePath();
            }
        }

        System.out.println("Файл " + filename + " не найден!");
        return filename;
    }

    private void setupUI() {
        drawingPanel = new DrawingPanel();

        JPanel buttons = new JPanel();
        JButton showBtn = new JButton("Показать диаметр");
        JButton resetBtn = new JButton("Сбросить отображение диаметра");
        JButton randomBtn = new JButton("Создать случайное дерево");
        JButton edgesBtn = new JButton("Загрузка дерева из списка рёбер");
        JButton matrixBtn = new JButton("Загрузка дерева из матрицы смежности");

        showBtn.addActionListener(e -> {
            showPath();
        });

        resetBtn.addActionListener(e -> {
            reset();
        });

        randomBtn.addActionListener(e -> {
            randomTree();
        });

        edgesBtn.addActionListener(e -> {
            String filePath = getFilePath("tree_edges.txt");
            loadFromEdgeList(filePath);
        });

        matrixBtn.addActionListener(e -> {
            String filePath = getFilePath("tree_adjacency.txt");
            loadFromAdjacencyMatrix(filePath);
        });

        buttons.add(showBtn);
        buttons.add(resetBtn);
        buttons.add(randomBtn);
        buttons.add(edgesBtn);
        buttons.add(matrixBtn);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        infoLabel = new JLabel();
        infoLabel.setFont(new Font("Dialog", Font.PLAIN, 16));
        infoPanel.add(infoLabel);

        add(drawingPanel, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        add(infoPanel, BorderLayout.EAST);
    }

    private void showPath() {
        System.out.println("Отображение пути диаметра");

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
        System.out.println("Сброс отображения");

        for (Vertex v : vertices) v.color = Color.BLUE;
        for (Edge e : edges) e.color = Color.BLACK;
        drawingPanel.repaint();
    }

    private void randomTree() {
        System.out.println("Генерация случайного дерева");

        Random rand = new Random();
        int n = 6 + rand.nextInt(6);
        System.out.println("Будет создано дерево с " + n + " вершинами");

        vertices.clear();
        edges.clear();
        diameterPath.clear();
        diameterLength = 0;
        diameterWeight = 0;

        for (int i = 0; i < n; i++) {
            vertices.add(new Vertex(i));
        }

        List<Integer> connected = new ArrayList<>();
        connected.add(0);

        for (int i = 1; i < n; i++) {
            int connectTo = connected.get(rand.nextInt(connected.size()));
            int weight = 1 + rand.nextInt(10);
            addEdge(connectTo, i, weight);
            connected.add(i);
            System.out.println("Добавлено ребро: " + connectTo + " - " + i + " (вес: " + weight + ")");
        }

        setPositions();
        findDiameter();
        drawingPanel.repaint();
        updateInfo();
        System.out.println("Случайное дерево создано");
    }

    private void updateInfo() {
        if (vertices.isEmpty()) {
            infoLabel.setText("<html>Нет вершин</html>");
            return;
        }

        StringBuilder pathStr = new StringBuilder();
        for (Vertex v : diameterPath) {
            pathStr.append(v.id).append(" ");
        }

        int pathWeight = calculatePathWeight(diameterPath);
        infoLabel.setText("<html>Вершин: " + vertices.size() +
                          "<br>Диаметр (количество рёбер): " + diameterLength +
                          "<br>Суммарный вес пути: " + pathWeight +
                          "<br>Путь: " + pathStr + "</html>");
    }

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
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 14));
                g2d.drawString(String.valueOf(v.id), v.x - 5, v.y + 5);
            }

            for (Edge edge : edges) {
                g2d.setColor(Color.RED);
                g2d.setFont(new Font("Arial", Font.BOLD, 20));
                int midX = (edge.u.x + edge.v.x) / 2;
                int midY = (edge.u.y + edge.v.y) / 2;
                g2d.drawString(edge.weightLabel, midX - 10, midY - 10);
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("Запуск программы поиска диаметра дерева");
        System.out.println("========================================");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Программа завершена");
            System.out.println("========================================");
        }));

        SwingUtilities.invokeLater(() -> {
            TreeDiameterSwing app = new TreeDiameterSwing();
            app.setVisible(true);
        });
    }
}