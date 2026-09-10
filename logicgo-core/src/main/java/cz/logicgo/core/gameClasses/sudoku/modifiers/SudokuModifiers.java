package cz.logicgo.core.gameClasses.sudoku.modifiers;


import cz.logicgo.core.gameClasses.sudoku.modifiers.SudokuModifierRecords.*;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.CompType;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.ParityType;

import java.util.ArrayList;
import java.util.List;


public class SudokuModifiers {

    private EvenOddModifier evenOdd;
    private SkyscraperModifier skyscraper;
    private GreaterThanModifier greaterThan;
    private VudokuModifier vudoku;
    private BetweenModifier between;
    private XvModifier xv;
    private ConsecutiveModifier consecutive;
    private SandwichModifier sandwich;
    private XSumsModifier xSums;
    private KillerModifier killer;
    private QuadruplesModifier quadruples;
    private GroupSumsModifier groupSums;
    private KropkiModifier kropki;

    public SudokuModifiers() {
    }

    public boolean hasEvenOdd() {
        return evenOdd != null;
    }

    public EvenOddModifier getEvenOdd() {
        return evenOdd;
    }

    public void setEvenOdd(EvenOddModifier evenOdd) {
        this.evenOdd = evenOdd;
    }

    public boolean hasSkyscraper() {
        return skyscraper != null;
    }

    public SkyscraperModifier getSkyscraper() {
        return skyscraper;
    }

    public void setSkyscraper(SkyscraperModifier skyscraper) {
        this.skyscraper = skyscraper;
    }

    public boolean hasSandwich() {
        return sandwich != null;
    }

    public SandwichModifier getSandwich() {
        return sandwich;
    }

    public void setSandwich(SandwichModifier sandwich) {
        this.sandwich = sandwich;
    }

    public boolean hasGreaterThan() {
        return greaterThan != null;
    }

    public GreaterThanModifier getGreaterThan() {
        return greaterThan;
    }

    public void setGreaterThan(GreaterThanModifier greaterThan) {
        this.greaterThan = greaterThan;
    }

    public boolean hasVudoku() {
        return vudoku != null;
    }

    public VudokuModifier getVudoku() {
        return vudoku;
    }

    public void setVudoku(VudokuModifier vudoku) {
        this.vudoku = vudoku;
    }

    public boolean hasBetween() {
        return between != null;
    }

    public BetweenModifier getBetween() {
        return between;
    }

    public void setBetween(BetweenModifier between) {
        this.between = between;
    }

    public boolean hasXv() {
        return xv != null;
    }

    public XvModifier getXv() {
        return xv;
    }

    public void setXv(XvModifier xv) {
        this.xv = xv;
    }

    public boolean hasConsecutive() {
        return consecutive != null;
    }

    public ConsecutiveModifier getConsecutive() {
        return consecutive;
    }

    public void setConsecutive(ConsecutiveModifier consecutive) {
        this.consecutive = consecutive;
    }

    public boolean hasKropki() {
        return kropki != null;
    }

    public KropkiModifier getKropki() {
        return kropki;
    }

    public void setKropki(KropkiModifier kropki) {
        this.kropki = kropki;
    }

    public void setXSums(XSumsModifier xSums) {
        this.xSums = xSums;
    }

    public XSumsModifier getXSums() {
        return xSums;
    }

    public boolean hasXSums() {
        return xSums != null;
    }

    public SudokuModifiers copy() {
        SudokuModifiers copy = new SudokuModifiers();

        if (this.hasEvenOdd()) {
            ParityType[][] p = this.evenOdd.parityTypes();
            ParityType[][] pCopy = new ParityType[p.length][];
            for (int i = 0; i < p.length; i++) pCopy[i] = p[i].clone();
            copy.setEvenOdd(new EvenOddModifier(pCopy));
        }

        if (this.hasSkyscraper()) {
            copy.setSkyscraper(new SkyscraperModifier(
                    this.skyscraper.top() != null ? this.skyscraper.top().clone() : null,
                    this.skyscraper.bottom() != null ? this.skyscraper.bottom().clone() : null,
                    this.skyscraper.left() != null ? this.skyscraper.left().clone() : null,
                    this.skyscraper.right() != null ? this.skyscraper.right().clone() : null
            ));
        }

        if (this.hasSandwich()) {
            copy.setSandwich(new SandwichModifier(
                    this.sandwich.top() != null ? this.sandwich.top().clone() : null,
                    this.sandwich.left() != null ? this.sandwich.left().clone() : null
            ));
        }

        if (this.hasGreaterThan()) {
            CompType[][] h = this.greaterThan.horizontal();
            CompType[][] hCopy = h != null ? new CompType[h.length][] : null;
            if (h != null) for (int i = 0; i < h.length; i++) hCopy[i] = h[i].clone();

            CompType[][] v = this.greaterThan.vertical();
            CompType[][] vCopy = v != null ? new CompType[v.length][] : null;
            if (v != null) for (int i = 0; i < v.length; i++) vCopy[i] = v[i].clone();

            copy.setGreaterThan(new GreaterThanModifier(hCopy, vCopy));
        }

        if (this.hasVudoku()) {
            copy.setVudoku(new VudokuModifier(new ArrayList<>(this.vudoku.marks())));
        }

        if (this.hasKropki()) {
            copy.setKropki(new KropkiModifier(this.kropki.dots()));
        }

        if (this.hasBetween()) {
            copy.setBetween(new BetweenModifier(new ArrayList<>(this.between.lines())));
        }

        if (this.hasXv()) {
            copy.setXv(new XvModifier(new ArrayList<>(this.xv.marks())));
        }

        if (this.hasConsecutive()) {
            copy.setConsecutive(new ConsecutiveModifier(new ArrayList<>(this.consecutive.cells())));
        }

        if (this.hasXSums()) {
            copy.setXSums(new XSumsModifier(
                    this.xSums.top() != null ? this.xSums.top().clone() : null,
                    this.xSums.bottom() != null ? this.xSums.bottom().clone() : null,
                    this.xSums.left() != null ? this.xSums.left().clone() : null,
                    this.xSums.right() != null ? this.xSums.right().clone() : null
            ));
        }

        if (this.hasKiller()) {
            List<KillerCage> cagesCopy = new ArrayList<>();
            if (this.killer.cages() != null) {
                for (KillerCage cage : this.killer.cages()) {
                    cagesCopy.add(new KillerCage(cage.targetSum(), new ArrayList<>(cage.cells())));
                }
            }
            copy.setKiller(new KillerModifier(cagesCopy));
        }

        if (this.hasQuadruples()) {
            List<QuadrupleMark> marksCopy = new ArrayList<>();
            if (this.quadruples.marks() != null) {
                for (QuadrupleMark mark : this.quadruples.marks()) {
                    marksCopy.add(new QuadrupleMark(mark.topLeft(), new ArrayList<>(mark.values())));
                }
            }
            copy.setQuadruples(new QuadruplesModifier(marksCopy));
        }

        if (this.hasGroupSums()) {
            List<GroupSumMark> marksCopy = new ArrayList<>();
            if (this.groupSums.marks() != null) {
                marksCopy.addAll(this.groupSums.marks());
            }
            copy.setGroupSums(new GroupSumsModifier(marksCopy));
        }

        return copy;
    }

    public boolean hasKiller() {
        return killer != null;
    }

    public boolean hasQuadruples() {
        return quadruples != null;
    }

    public boolean hasGroupSums() {
        return groupSums != null;
    }

    public KillerModifier getKiller() {
        return killer;
    }

    public SudokuModifiers setKiller(KillerModifier killer) {
        this.killer = killer;
        return this;
    }

    public QuadruplesModifier getQuadruples() {
        return quadruples;
    }

    public SudokuModifiers setQuadruples(QuadruplesModifier quadruples) {
        this.quadruples = quadruples;
        return this;
    }

    public GroupSumsModifier getGroupSums() {
        return groupSums;
    }

    public SudokuModifiers setGroupSums(GroupSumsModifier groupSums) {
        this.groupSums = groupSums;
        return this;
    }

    public List<SudokuModifier> getActiveModifiers() {
        List<SudokuModifier> active = new ArrayList<>();

        if (evenOdd != null) active.add(evenOdd);
        if (skyscraper != null) active.add(skyscraper);
        if (greaterThan != null) active.add(greaterThan);
        if (vudoku != null) active.add(vudoku);
        if (between != null) active.add(between);
        if (xv != null) active.add(xv);
        if (consecutive != null) active.add(consecutive);
        if (sandwich != null) active.add(sandwich);
        if (xSums != null) active.add(xSums);
        if (killer != null) active.add(killer);
        if (quadruples != null) active.add(quadruples);
        if (groupSums != null) active.add(groupSums);
        if (kropki != null) active.add(kropki);

        return active;
    }

    public boolean atLeastOneActive() {
        return !getActiveModifiers().isEmpty();
    }
}
