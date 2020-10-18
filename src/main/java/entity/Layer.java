package entity;

public class Layer {

    public int cellSidePixelCount;

    public Cell[][] cells;

    public Layer(int layerIndex, int cellSidePixelCount, int w, int h){
        this.cellSidePixelCount = cellSidePixelCount;
        this.cells = new Cell[w][h];
        for(int i = 0; i < w; i++)
            for(int j = 0; j < h; j++)
            {
                Index index = new Index(layerIndex, i, j);
                cells[i][j] = new Cell(index, null, cellSidePixelCount);
            }

    }
}
