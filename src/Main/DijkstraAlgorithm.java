package Main;

public class DijkstraAlgorithm {


    public static void main(String[] args) {

        int graph[][] = new int[][]{{0, 4, 0, 0, 0, 0, 0, 8, 0},
                {4, 0, 8, 0, 0, 0, 0, 11, 0},
                {0, 8, 0, 7, 0, 4, 0, 0, 2},
                {0, 0, 7, 0, 9, 14, 0, 0, 0},
                {0, 0, 0, 9, 0, 10, 0, 0, 0},
                {0, 0, 4, 14, 10, 0, 2, 0, 0},
                {0, 0, 0, 0, 0, 2, 0, 1, 6},
                {8, 11, 0, 0, 0, 0, 1, 0, 7},
                {0, 0, 2, 0, 0, 0, 6, 7, 0}
        };


        int [][] basic = new int[][]{
                {0,2,Integer.MAX_VALUE},
                {2,0,1},
                {7,1,0}
        };

        int source = 0;
        int des = 4;

        bellman_ford(basic);

        dijkstra(graph);
    }

    public static int[] bellman_ford(int[][] graph, int source){
        int[] d= new int[graph.length];
        int[] p = new int[graph.length];

        for (int i = 0; i < graph.length; i++) {
            d[i]= graph[source][i];
            p[i]=0;
        }

        d[source]=0;

            for (int i = 0; i < graph.length; i++) {
                for (int j = 0; j < graph[i].length; j++) {
                    if(d[i]!=Integer.MAX_VALUE && graph[i][j]!=Integer.MAX_VALUE){
                        if(d[j]> d[i] + graph[i][j])
                            d[j]=d[i]+graph[i][j];
                    }

                }
            }

        return d;

    }

    public static void bellman_ford(int[][] graph){
        int[][] dv= new int[graph.length][graph.length];

        for (int i = 0; i < graph.length; i++) {
            dv[i]=bellman_ford(graph,i);
        }

        print(dv);

    }

    //use dictionary  i think hash map for java
    public static int[] dijkstra(int[][] graph, int source){
        int[] d= new int[graph.length];
        boolean[] visited = new boolean[graph.length];

        for (int i = 0; i < graph.length; i++) {
            d[i]=Integer.MAX_VALUE;
            visited[i]=false;
        }

        d[source]=0;
        //find the shortest path for all vertices
        for (int i = 0; i < graph.length - 1; i++) {
            int min = findMinIdex(d,visited);
            visited[min]=true;

            for (int j = 0; j < graph.length; j++) {
                if (!visited[j] && graph[min][j]!=0 && d[min] != Integer.MAX_VALUE && d[min]+graph[min][j] < d[j]){
                    d[j]= d[min]+graph[min][j];
                }
            }

        }
        return d;
    }

    public static void dijkstra(int[][]graph){
        for (int i = 0; i < graph.length; i++) {
            print(dijkstra(graph,i));
        }
    }

    public static int findMinIdex(int[] dist, boolean[] visited){
        int min = Integer.MAX_VALUE;
        int index= 0;
        for (int i = 0; i < dist.length; i++) {
            if(min>=dist[i] && visited[i]==false){
                min=dist[i];
                index=i;
            }
        }
        return index;
    }

    public static void print(int[] a){
        for (int i = 0; i < a.length; i++) {
            System.out.print(a[i]+ " ");
        }
        System.out.println();
    }

    public static void print(int[][] a){
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[i].length; j++) {
                System.out.print(a[i][j]+" ");
            }
            System.out.println();
        }
    }
}
