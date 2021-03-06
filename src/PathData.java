import client.WorldModel;

/**
 * Created by Amir Shams on 11/29/2015.
 */
public class PathData
{
    int [][] distance;
    String [][] parent;
    int width,height;
    Position source;
    Position dest;
    int maxINF=10000;

    int xMoves[] = {0,0,1,-1};
    int yMoves[] = {1,-1,0,0};
    String directions[] = {"S", "N", "E", "W"};

    private boolean passable(int x,int y)
    {
        if(x < 0 || y < 0 || x >= parent.length || y >= parent[0].length)
            return false;

        if(distance[x][y] != maxINF)
            return false;

        return true;
    }

    public PathData(int[][] matrix,Position source)
    {
        this.source = source;
        this.width = matrix.length;
        this.height = matrix[0].length;

        parent = new String[width][height];
        distance = new int[width][height];
        for(int i=0;i<width;i++)
            for(int j=0;j<height;j++)
            {
                distance[i][j] = maxINF; //inf
                parent[i][j] = "";
            }

        distance[source.x][source.y] = 0;
    }
    public String toPath(Position destination)
    {
        String path = "";

        while(!destination.equals(source))
        {
            String move = parent[destination.x][destination.y];
            path += move;
            if(move == "S")
                destination = new Position(destination.x,destination.y + 1);
            else if(move == "N")
                destination = new Position(destination.x,destination.y - 1);
            else if(move == "W")
                destination = new Position(destination.x - 1,destination.y);
            else if(move == "E")
                destination = new Position(destination.x + 1,destination.y);
            else
                break;
        }
        if(path.equals(""))
        {
            for(int i=0;i<xMoves.length;i++)
                if(passable(source.x,source.y) && parent[source.x + xMoves[i]][source.y + yMoves[i]].equals(""))
                    return parent[source.x + xMoves[i]][source.y + yMoves[i]];
        }
        return new StringBuilder(path).reverse().toString();
    }

    public String toPath()
    {
        Position destination=dest;
        String path = "";

        while(!destination.equals(source))
        {
            String move = parent[destination.x][destination.y];
            System.err.println("//"+destination.x+" "+destination.y);

            path += move;
            if(move == "S")
                destination = new Position(destination.x,destination.y + 1);
            else if(move == "N")
                destination = new Position(destination.x,destination.y -1);
            else if(move == "W")
                destination = new Position(destination.x -1,destination.y);
            else if(move == "E")
                destination = new Position(destination.x + 1,destination.y);
            else
                break;
        }

        return new StringBuilder(path).reverse().toString();
    }

}
