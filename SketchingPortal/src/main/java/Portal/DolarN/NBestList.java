package Portal.DolarN;

import java.util.ArrayList;

/**
 * Class holding a list of the Recognition Results
 */
public class NBestList
{
    /**
     * List of the Results
     */
    private ArrayList<NBestResult> _nBestList;

    /**
     * Empty Constructor
     */
    public NBestList()
    {
        _nBestList = new ArrayList<NBestResult>();
    }

    /**
     * getter of the List of Results
     * @return List of Results
     */
    public ArrayList<NBestResult> getNBestList(){
        return this._nBestList;
    }

    /**
     * Function adding a new Result to the current List
     * @param name Name of the New Result
     * @param score Score of the New Result
     */
    public void AddResult(String name, double score) {
        NBestResult r = new NBestResult(name, score);
        _nBestList.add(r);
    }

    /**
     * Function sorting Descending the Results, by score.
     * the Result found on position 0 will have the best fit and thus will represent the actual result of the recognition.
     */
    public void SortDescending() {
        for(int i = 0 ; i < _nBestList.size() - 1 ; i++){
            for(int j = i + 1 ; j < _nBestList.size() ; j++){
                if(_nBestList.get(i).getScore() < _nBestList.get(j).getScore()){
                    NBestResult aux = new NBestResult(_nBestList.get(i));

                    _nBestList.get(i).setName(_nBestList.get(j).getName());
                    _nBestList.get(i).setScore(_nBestList.get(j).getScore());

                    _nBestList.get(j).setName(aux.getName());
                    _nBestList.get(j).setScore(aux.getScore());
                }
            }
        }
    }

    /**
     * Returns the Name of the Top (Best fit) Result
     * @return Name of the Best Result
     */
    public String getNameOfTopResult() {
        if (_nBestList.size() > 0) {
            NBestResult r = (NBestResult) _nBestList.get(0);
            return r.getName();
        }
        return "";
    }

    /**
     * Returns the Score of the Top (Best fit) Result
     * @return Name of the Best Result
     */
    public double getScoreOfTopResult() {
        if (_nBestList.size() > 0)
        {
            NBestResult r = (NBestResult) _nBestList.get(0);
            return r.getScore();
        }
        return -1.0;
    }
}