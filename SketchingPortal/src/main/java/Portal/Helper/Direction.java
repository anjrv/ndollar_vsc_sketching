package Portal.Helper;

/**
 * Class holding the 16 trivial Cardinal Directions
 *  Where each direction has certain features
 *      The functions (features) are documented after all the 16 directions are defined.
 */
public enum Direction {
    /**
     * NORTH Direction
     */
    N(){
        public int getIndex(){
            return 0;
        }

        public String getDirectionName(){
            return "NORTH";
        }

        public String getDirectionNameAbbreviated(){
            return "N";
        }

        public double getLowerDegreeLimit(){ return 348.75; }

        public double getMiddleDirection(){ return 0;}

        public double getHigherDegreeLimit(){
            return 11.25;
        }

        public boolean isInBetweenLimits(double degrees){
            return degrees > this.getLowerDegreeLimit() || degrees <= this.getHigherDegreeLimit();
        }
    },

    /**
     * NORTH - NORTH-EAST Direction
     */
    NNE(){
        public int getIndex(){
            return 1;
        }

        public String getDirectionName(){
            return "NORTH-NORTH-EAST";
        }

        public String getDirectionNameAbbreviated(){
            return "NNE";
        }

        public double getLowerDegreeLimit(){
            return 11.25;
        }

        public double getHigherDegreeLimit(){
            return 33.75;
        }

        public double getMiddleDirection(){ return (getLowerDegreeLimit()+getHigherDegreeLimit())/2;}

        public boolean isInBetweenLimits(double degrees){ return degrees > this.getLowerDegreeLimit() && degrees <= this.getHigherDegreeLimit(); }
    },

    /**
     * NORTH-EAST Direction
     */
    NE(){
        public int getIndex(){
            return 2;
        }

        public String getDirectionName(){
            return "NORTH-EAST";
        }

        public String getDirectionNameAbbreviated(){
            return "NE";
        }

        public double getLowerDegreeLimit(){
            return 33.75;
        }

        public double getHigherDegreeLimit(){
            return 56.25;
        }

        public double getMiddleDirection(){ return (getLowerDegreeLimit()+getHigherDegreeLimit())/2;}

        public boolean isInBetweenLimits(double degrees){
            return degrees > this.getLowerDegreeLimit() && degrees <= this.getHigherDegreeLimit();
        }
    },

    /**
     * EAST - NORTH-EAST Direction
     */
    ENE(){
        public int getIndex(){
            return 3;
        }

        public String getDirectionName(){
            return "EAST-NORTH-EAST";
        }

        public String getDirectionNameAbbreviated(){
            return "ENE";
        }

        public double getLowerDegreeLimit(){
            return 56.25;
        }

        public double getHigherDegreeLimit(){
            return 78.75;
        }

        public double getMiddleDirection(){ return (getLowerDegreeLimit()+getHigherDegreeLimit())/2;}

        public boolean isInBetweenLimits(double degrees){ return degrees > this.getLowerDegreeLimit() && degrees <= this.getHigherDegreeLimit(); }
    },

    /**
     * EAST Direction
     */
    E(){
        public int getIndex(){
            return 4;
        }

        public String getDirectionName(){
            return "EAST";
        }

        public String getDirectionNameAbbreviated(){
            return "E";
        }

        public double getLowerDegreeLimit(){
            return 78.75;
        }

        public double getHigherDegreeLimit(){
            return 101.25;
        }

        public double getMiddleDirection(){ return (getLowerDegreeLimit()+getHigherDegreeLimit())/2;}

        public boolean isInBetweenLimits(double degrees){ return degrees > this.getLowerDegreeLimit() && degrees <= this.getHigherDegreeLimit(); }
    },

    /**
     * EAST - SOUTH-EAST Direction
     */
    ESE(){
        public int getIndex(){
            return 5;
        }

        public String getDirectionName(){
            return "EAST-SOUTH-EAST";
        }

        public String getDirectionNameAbbreviated(){
            return "ESE";
        }

        public double getLowerDegreeLimit(){
            return 101.25;
        }

        public double getHigherDegreeLimit(){
            return 123.75;
        }

        public double getMiddleDirection(){ return (getLowerDegreeLimit()+getHigherDegreeLimit())/2;}

        public boolean isInBetweenLimits(double degrees){ return degrees > this.getLowerDegreeLimit() && degrees <= this.getHigherDegreeLimit(); }
    },

    /**
     * SOUTH-EAST Direction
     */
    SE(){
        public int getIndex(){
            return 6;
        }

        public String getDirectionName(){
            return "SOUTH-EAST";
        }

        public String getDirectionNameAbbreviated(){
            return "SE";
        }

        public double getLowerDegreeLimit(){
            return 123.75;
        }

        public double getHigherDegreeLimit(){
            return 146.25;
        }

        public double getMiddleDirection(){ return (getLowerDegreeLimit()+getHigherDegreeLimit())/2;}

        public boolean isInBetweenLimits(double degrees){ return degrees > this.getLowerDegreeLimit() && degrees <= this.getHigherDegreeLimit(); }
    },

    /**
     * SOUTH - SOUTH-EAST Direction
     */
    SSE(){
        public int getIndex(){
            return 7;
        }

        public String getDirectionName(){
            return "SOUTH-SOUTH-EAST";
        }

        public String getDirectionNameAbbreviated(){
            return "SSE";
        }

        public double getLowerDegreeLimit(){
            return 146.25;
        }

        public double getHigherDegreeLimit(){
            return 168.75;
        }

        public double getMiddleDirection(){ return (getLowerDegreeLimit()+getHigherDegreeLimit())/2;}

        public boolean isInBetweenLimits(double degrees){ return degrees > this.getLowerDegreeLimit() && degrees <= this.getHigherDegreeLimit(); }
    },

    /**
     * SOUTH Direction
     */
    S(){
        public int getIndex(){
            return 8;
        }

        public String getDirectionName(){
            return "SOUTH";
        }

        public String getDirectionNameAbbreviated(){
            return "S";
        }

        public double getLowerDegreeLimit(){
            return 168.75;
        }

        public double getHigherDegreeLimit(){
            return 191.25;
        }

        public double getMiddleDirection(){ return (getLowerDegreeLimit()+getHigherDegreeLimit())/2;}

        public boolean isInBetweenLimits(double degrees){ return degrees > this.getLowerDegreeLimit() && degrees <= this.getHigherDegreeLimit(); }
    },

    /**
     * SOUTH - SOUTH-WEST Direction
     */
    SSW(){
        public int getIndex(){
            return 9;
        }

        public String getDirectionName(){
            return "SOUTH-SOUTH-WEST";
        }

        public String getDirectionNameAbbreviated(){
            return "SSW";
        }

        public double getLowerDegreeLimit(){
            return 191.25;
        }

        public double getHigherDegreeLimit(){
            return 213.75;
        }

        public double getMiddleDirection(){ return (getLowerDegreeLimit()+getHigherDegreeLimit())/2;}

        public boolean isInBetweenLimits(double degrees){ return degrees > this.getLowerDegreeLimit() && degrees <= this.getHigherDegreeLimit(); }
    },

    /**
     * SOUTH-WEST Direction
     */
    SW(){
        public int getIndex(){
            return 10;
        }

        public String getDirectionName(){
            return "SOUTH-WEST";
        }

        public String getDirectionNameAbbreviated(){
            return "SW";
        }

        public double getLowerDegreeLimit(){
            return 213.75;
        }

        public double getHigherDegreeLimit(){
            return 236.25;
        }

        public double getMiddleDirection(){ return (getLowerDegreeLimit()+getHigherDegreeLimit())/2;}

        public boolean isInBetweenLimits(double degrees){ return degrees > this.getLowerDegreeLimit() && degrees <= this.getHigherDegreeLimit(); }
    },

    /**
     * WEST - SOUTH-WEST Direction
     */
    WSW(){
        public int getIndex(){
            return 11;
        }

        public String getDirectionName(){
            return "WEST-SOUTH-WEST";
        }

        public String getDirectionNameAbbreviated(){
            return "WSW";
        }

        public double getLowerDegreeLimit(){ return 236.25; }

        public double getHigherDegreeLimit(){ return 258.75; }

        public double getMiddleDirection(){ return (getLowerDegreeLimit()+getHigherDegreeLimit())/2;}

        public boolean isInBetweenLimits(double degrees){ return degrees > this.getLowerDegreeLimit() && degrees <= this.getHigherDegreeLimit(); }
    },

    /**
     * WEST Direction
     */
    W(){
        public int getIndex(){
            return 12;
        }

        public String getDirectionName(){
            return "WEST";
        }

        public String getDirectionNameAbbreviated(){
            return "W";
        }

        public double getLowerDegreeLimit(){
            return 258.75;
        }

        public double getHigherDegreeLimit(){
            return 281.25;
        }

        public double getMiddleDirection(){ return (getLowerDegreeLimit()+getHigherDegreeLimit())/2;}

        public boolean isInBetweenLimits(double degrees){ return degrees > this.getLowerDegreeLimit() && degrees <= this.getHigherDegreeLimit(); }
    },

    /**
     * WEST - NORTH-WEST Direction
     */
    WNW(){
        public int getIndex(){
            return 13;
        }

        public String getDirectionName(){
            return "WEST-NORTH-WEST";
        }

        public String getDirectionNameAbbreviated(){
            return "WNW";
        }

        public double getLowerDegreeLimit(){
            return 281.25;
        }

        public double getHigherDegreeLimit(){
            return 303.75;
        }

        public double getMiddleDirection(){ return (getLowerDegreeLimit()+getHigherDegreeLimit())/2;}

        public boolean isInBetweenLimits(double degrees){ return degrees > this.getLowerDegreeLimit() && degrees <= this.getHigherDegreeLimit(); }
    },

    /**
     * NORTH-WEST Direction
     */
    NW(){
        public int getIndex(){
            return 14;
        }

        public String getDirectionName(){
            return "NORTH-WEST";
        }

        public String getDirectionNameAbbreviated(){
            return "NW";
        }

        public double getLowerDegreeLimit(){
            return 303.75;
        }

        public double getHigherDegreeLimit(){
            return 326.25;
        }

        public double getMiddleDirection(){ return (getLowerDegreeLimit()+getHigherDegreeLimit())/2;}

        public boolean isInBetweenLimits(double degrees){ return degrees > this.getLowerDegreeLimit() && degrees <= this.getHigherDegreeLimit(); }
    },

    /**
     * NORTH - NORTH-WEST Direction
     */
    NNW(){
        public int getIndex(){
            return 15;
        }

        public String getDirectionName(){
            return "NORTH-NORTH-WEST";
        }

        public String getDirectionNameAbbreviated(){ return "NNW"; }

        public double getLowerDegreeLimit(){
            return 326.25;
        }

        public double getHigherDegreeLimit(){
            return 348.75;
        }

        public double getMiddleDirection(){ return (getLowerDegreeLimit()+getHigherDegreeLimit())/2;}

        public boolean isInBetweenLimits(double degrees){ return degrees > this.getLowerDegreeLimit() && degrees <= this.getHigherDegreeLimit(); }
    };

    /**
     * variables containing default numbers used in this function
     */
    private static int  numberOfDirections = 16,
                        numberOfMaximumDegrees = 360;

    /**
     * @return The number of directions (16 trivial directions).
     */
    public static int getNumberOfDirections() {
        return numberOfDirections;
    }

    /**
     * @return the maximum number of degrees (it goes up to 360 degrees)
     */
    public static int getNumberOfMaximumDegrees(){ return numberOfMaximumDegrees;}

    /**
     * @return The index of a direction
     */
    public abstract int getIndex();

    /**
     * Get the complete name of a direction
     * @return The complete name (in upper case, and using "-" as deliminator)
     */
    public abstract String getDirectionName();

    /**
     * Get the abbreviated version of a direction
     * @return The abbreviated name (without any deliminators "-" )
     */
    public abstract String getDirectionNameAbbreviated();

    /**
     * Get the Lower Limit (in degrees) of a direction
     * @return The Lower Limit
     */
    public abstract double getLowerDegreeLimit();

    /**
     * Get the Higher Limit (in degrees) of a direction
     * @return The Higher Limit
     */
    public abstract double getHigherDegreeLimit();

    /**
     * Get the "General Direction" of a directions
     * @return the General (Middle) direction
     */
    public abstract double getMiddleDirection();

    /**
     * Checks if a given value (in degrees) is included in a direction
     * @param degrees Tested value (in degrees)
     * @return a Boolean value representing weather the provided value is included in a direction or not.
     */
    public abstract boolean isInBetweenLimits(double degrees);

    /**
     * Returns the Direction based on a provided index
     * @param index Index of direction
     * @return The representative Direction
     */
    public static Direction getDirectionByIndex(int index){
        var indexMod = index % getNumberOfDirections();
        switch (indexMod){
            case 0: return N;
            case 1: return NNE;
            case 2: return NE;
            case 3: return ENE;
            case 4: return E;
            case 5: return ESE;
            case 6: return SE;
            case 7: return SSE;
            case 8: return S;
            case 9: return SSW;
            case 10: return SW;
            case 11: return WSW;
            case 12: return W;
            case 13: return WNW;
            case 14: return NW;
            default: return NNW;
        }
    }

    /**
     *  Returns the Direction based on a provided value (in degrees)
     * @param degrees The provided value (in degrees)
     * @return The representative Direction
     */
    public static Direction getDirectionByDegrees(double degrees){
        var degreesMod = degrees % 360;
        if(N.isInBetweenLimits(degreesMod))     return N;
        if(NNE.isInBetweenLimits(degreesMod))   return NNE;
        if(NE.isInBetweenLimits(degreesMod))    return NE;
        if(ENE.isInBetweenLimits(degreesMod))   return ENE;
        if(E.isInBetweenLimits(degreesMod))     return E;
        if(ESE.isInBetweenLimits(degreesMod))   return ESE;
        if(SE.isInBetweenLimits(degreesMod))    return SE;
        if(SSE.isInBetweenLimits(degreesMod))   return SSE;
        if(S.isInBetweenLimits(degreesMod))     return S;
        if(SSW.isInBetweenLimits(degreesMod))   return SSW;
        if(SW.isInBetweenLimits(degreesMod))    return SW;
        if(WSW.isInBetweenLimits(degreesMod))   return WSW;
        if(W.isInBetweenLimits(degreesMod))     return W;
        if(WNW.isInBetweenLimits(degreesMod))   return WNW;
        if(NW.isInBetweenLimits(degreesMod))    return NW;
                                                return NNW;
    }

    /**
     *  Returns the Direction based on a provided abbreviated string
     * @param abbreviatedName The provided abbreviated name
     * @return The representative Direction
     */
    public static Direction getDirectionByName(String abbreviatedName){
        switch (abbreviatedName){
            case "N": return N;
            case "NNE": return NNE;
            case "NE": return NE;
            case "ENE": return ENE;
            case "E": return E;
            case "ESE": return ESE;
            case "SE": return SE;
            case "SSE": return SSE;
            case "S": return S;
            case "SSW": return SSW;
            case "SW": return SW;
            case "WSW": return WSW;
            case "W": return W;
            case "WNW": return WNW;
            case "NW": return NW;
            default: return NNW;
        }
    }
}