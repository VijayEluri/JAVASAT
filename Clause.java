/*
 * Stores the variables contained in each Clause of the Formula
 * To be Stored in HashObjects two ArrayLists
 */


/**
 *
 * @author Trevor Stevens
 */

public class Clause {
    private int variables[];
    private int size,length,i,found;

    public Clause(int size, int a[]){
        variables = a;
        length = size;
        this.size = size;
    }

    public int size(){
        return size;
    }

    public int actualSize(){
      return length;
    }

    public void removeVar(int var){
        //remove and place a 0 in place of the variable in the array to hold position
        //also update clause size with the update size of the array
        for(i=0; i<length; i++){
            if(variables[i] == var){
                variables[i] = 0;
                size--;
            }
        }
    }

    public void addVar(int var){
        boolean notSet = true;
        for(i=0; i<length && notSet;i++){
            if(variables[i] == 0){
                variables[i] = var;
                notSet = false;
                size++;
            }
        }
    }

    public int get(int index){
        //*ensure index is formated for array*
        return variables[index];
    }

    public int lengthOne(){
        //return (size == 1) ? findOne() : 0;
        if(size == 1){
            return findOne();
        } else {
            return 0;
        }
    }

    private int findOne(){
        for(i =0; i<length;i++){
            //found = variables[i] != 0 ? variables[i] : ;
            if(variables[i] != 0){
                return variables[i];
            }
        }
        return 0; //base case sould ever happen.
    }

    @Override
    public String toString(){
        String returnString = "";
        for(i = 0; i< variables.length; i++){
            returnString += variables[i] + " ";
        }
        return returnString;
    }

}
