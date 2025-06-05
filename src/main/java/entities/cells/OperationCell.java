package entities.cells;

import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxStyleUtils;
import controllers.ConstantController;
import database.RowConverter;
import entities.Column;
import entities.Edge;
import entities.utils.TreeUtils;
import entities.utils.cells.CellUtils;
import static entities.utils.cells.CellUtils.changeCellName;
import enums.ColumnDataType;
import enums.OperationArity;
import enums.OperationErrorType;
import static enums.OperationErrorType.SAME_SOURCE;
import enums.OperationType;
import gui.frames.ErrorFrame;
import gui.frames.forms.operations.IOperationForm;
import gui.frames.main.MainFrame;
import ibd.query.Operation;
import ibd.query.ReferedDataSource;
import ibd.query.SingleSource;
import ibd.query.unaryop.Reference;
import operations.IOperator;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class OperationCell extends Cell {

    private final OperationType type;

    private List<Cell> parents;

    private final OperationArity arity;

    private final Class<? extends IOperationForm> form;

    private List<String> arguments;

    private Boolean error;

    private String errorMessage;

    private final Class<? extends IOperator> operatorClass;

    private Boolean hasBeenInitialized;

    public OperationCell(mxCell jCell, OperationType type) {
        super(
                type.getFormattedDisplayName(), jCell, ConstantController.OPERATION_CELL_HEIGHT);

        this.parents = new ArrayList<>();
        this.arguments = new ArrayList<>();
        this.error = false;
        this.errorMessage = null;
        this.hasBeenInitialized = false;
        this.type = type;
        this.arity = type.arity;
        this.form = type.form;
        this.operatorClass = type.operatorClass;
    }

    public OperationCell(mxCell jCell, OperationType type, List<Cell> parents, List<String> arguments, String alias) {
        this(jCell, type);

        this.arguments = arguments;
        this.alias = alias;

        // if (parents != null && !parents.isEmpty())

        {
            this.hasBeenInitialized = true;
            this.parents = parents;

            parents.forEach(parent -> {
                parent.setChild(this);
                MainFrame.getGraph().insertEdge(parent.getJCell(), null, "", parent.getJCell(), jCell);
            });

            this.updateOperation();
        }
    }

    public void editOperation(mxCell jCell) {
        if (!(this.hasBeenInitialized || type == OperationType.CONDITION || type == OperationType.REFERENCE)) {
            return;
        }

        try {
            Constructor<? extends IOperationForm> constructor = this.form.getDeclaredConstructor(mxCell.class);
            constructor.newInstance(jCell);
        } catch (InstantiationException | IllegalAccessException
                | NoSuchMethodException | InvocationTargetException exception) {
            new ErrorFrame(ConstantController.getString("error"));
        }
    }

    public boolean hasBeenInitialized() {
        return this.hasBeenInitialized;
    }

    public void updateOperation() {
        if (!(this.hasBeenInitialized || type == OperationType.CONDITION)) {
            return;
        }

        try {
            Constructor<? extends IOperator> constructor = this.operatorClass.getDeclaredConstructor();
            IOperator operation = constructor.newInstance();
            operation.executeOperation(this.getJCell(), this.getArguments(), getAlias());
        } catch (InstantiationException | IllegalAccessException
                | NoSuchMethodException | InvocationTargetException exception) {
            new ErrorFrame(ConstantController.getString("error"));
        }
    }

    public void setArguments(List<String> arguments) {
        this.hasBeenInitialized = true;

        if (arguments != null && this.arguments != null) {
            this.arguments = new ArrayList<>(arguments);
        }
    }

    public List<String> getArguments() {
        return this.arguments;
    }

    public OperationType getType() {
        return this.type;
    }

    @Override
    public boolean alwaysAllowConnections() {
        if (this.type != null)
            return this.type.allwaysAllowConnections;
        return super.alwaysAllowConnections();
    }

    public OperationArity getArity() {
        return this.arity;
    }

    @Override
    public List<Cell> getParents() {
        return this.parents;
    }

    public void addParent(Cell cell) {
        this.parents.add(cell);
    }

    public void removeParent(Cell cell) {
        this.parents.remove(cell);
    }

    public void removeParent(mxCell jCell) {
        Optional<Cell> optionalCell = CellUtils.getActiveCell(jCell);

        optionalCell.ifPresent(this::removeParent);
    }

    public void removeParent(Edge edge) {
        this.removeParent(edge.getParent());
    }

    public void removeParents() {
        this.parents.clear();
    }

    @Override
    public boolean hasParents() {
        return !this.parents.isEmpty();
    }

    public Boolean hasTree() {
        return this.getOperator() != null;
    }

    private void setError() {
        String style = this.getJCell().getStyle();

        style = mxStyleUtils.setStyle(style, mxConstants.STYLE_STROKECOLOR, "red");
        style = mxStyleUtils.setStyle(style, mxConstants.STYLE_FONTCOLOR, "red");

        MainFrame.getGraph().getModel().setStyle(this.getJCell(), style);

        this.error = true;
    }

    public void setError(OperationErrorType message) {

        String style = this.getJCell().getStyle();

        style = mxStyleUtils.setStyle(style, mxConstants.STYLE_STROKECOLOR, "red");
        style = mxStyleUtils.setStyle(style, mxConstants.STYLE_FONTCOLOR, "red");

        MainFrame.getGraph().getModel().setStyle(this.getJCell(), style);

        this.error = true;

        this.errorMessage = switch (message) {
            case NO_ONE_ARGUMENT ->
                ConstantController.getString("cell.operationCell.error.noOneArgument");
            case NO_ONE_PARENT ->
                ConstantController.getString("cell.operationCell.error.noOneParent");
            case NO_PARENT ->
                ConstantController.getString("cell.operationCell.error.noParent");
            case NULL_ARGUMENT ->
                ConstantController.getString("cell.operationCell.error.nullArgument");
            case PARENT_ERROR ->
                ConstantController.getString("cell.operationCell.error.parentError");
            case PARENT_WITHOUT_COLUMN ->
                ConstantController.getString("cell.operationCell.error.parentWithoutColumn");
            case NO_TWO_PARENTS ->
                ConstantController.getString("cell.operationCell.error.noTwoParents");
            case NO_TWO_ARGUMENTS ->
                ConstantController.getString("cell.operationCell.error.noTwoArguments");
            case EMPTY_ARGUMENT ->
                ConstantController.getString("cell.operationCell.error.emptyArgument");
            case NO_PREFIX ->
                ConstantController.getString("cell.operationCell.error.noPrefix");
            case SAME_SOURCE ->
                ConstantController.getString("cell.operationCell.error.sameSource");
            case NOT_SOURCE ->
                ConstantController.getString("cell.operationCell.error.notSource");
        };
    }

    public void setError(String message) {
        this.setError();
        this.errorMessage = message;
    }

    public void removeError() {
        MainFrame.getGraph().getModel().setStyle(this.getJCell(), this.getStyle());
        MainFrame.getGraphComponent().clearCellOverlays();

        this.error = false;
        this.errorMessage = null;
    }

    public void reset() {
        this.name = this.type.getFormattedDisplayName();
        this.parents.clear();
        this.arguments.clear();
        this.hasBeenInitialized = false;

        this.removeError();

        MainFrame.getGraph().getModel().setValue(this.jCell, this.name);
    }

    public OperationCell copy() {
        OperationCell operationCell = new OperationCell(
                this.jCell, this.type, new ArrayList<>(this.parents), new ArrayList<>(this.arguments), getAlias());

        operationCell.name = this.name;
        operationCell.alias = this.alias;
        operationCell.errorMessage = this.getErrorMessage();
        operationCell.error = this.error;
        operationCell.hasBeenInitialized = this.hasBeenInitialized;

        return operationCell;
    }

    public void updateFrom(OperationCell cell) {
        this.name = cell.name;
        this.alias = cell.alias;
        this.parents = new ArrayList<>(cell.parents);
        this.arguments = new ArrayList<>(cell.arguments);
        this.hasBeenInitialized = cell.hasBeenInitialized;
        this.error = cell.error;
        this.errorMessage = cell.errorMessage;
    }

    @Override
    public boolean hasError() {
        return this.error;
    }

    public String getErrorMessage() {
        return this.hasError() ? this.errorMessage : ConstantController.getString("cell.operationCell.error.noError");
    }

    public List<Column> getColumns() {

        if (operator != null && operator instanceof Reference) {
            setColumns();
        }
        return this.columns;

    }

    public void setColumns() {
        // List<Column> columns = new ArrayList<>();
        columns.clear();
        ReferedDataSource dataSources[] = null;
        try {
            this.getOperator().prepareAllDataSources();
            dataSources = this.getOperator().getExposedDataSources();
        } catch (Exception ex) {
        }
        for (int i = 0; i < dataSources.length; i++) {
            ReferedDataSource dataSource = dataSources[i];
            List<ibd.table.prototype.column.Column> sourceColumns = dataSource.prototype.getColumns();
            for (int j = 0; j < sourceColumns.size(); j++) {
                ibd.table.prototype.column.Column col = sourceColumns.get(j);
                ColumnDataType dataType = RowConverter.convertDataType(col);
                Column column = new Column(col.getName(), dataSource.alias, dataType, false);
                columns.add(column);
            }
        }

        // this.columns = columns;
    }

    public void setColumnsOld() {
        List<Column> columns = new ArrayList<>();

        for (Map.Entry<String, List<String>> contentInfo : this.getOperator().getContentInfo().entrySet()) {
            for (String columnName : contentInfo.getValue()) {
                Column column = new Column(columnName, contentInfo.getKey(), ColumnDataType.NONE, false);

                for (Cell parent : this.parents) {
                    Column finalColumn = column;
                    column = parent.getColumns().stream().filter(c -> c.equals(finalColumn)).findAny().orElse(column);
                }

                columns.add(column);
            }
        }

        this.columns = columns;
    }

    @Override
    public boolean hasParentErrors() {
        boolean error = false;

        for (Cell cell : this.parents) {
            if (cell.hasError()) {
                error = true;
            }
        }

        return error;
    }

    @Override
    public void setOperator(Operation operator) {
        this.operator = operator;
        this.setColumns();
    }

    public void asOperator(String newName) {
        // if (!(operator instanceof SingleSource)) return;
        operator.setDataSourceAlias(newName);
        this.alias = newName;
        jCell.setValue(newName);
        adjustWidthSize();
        if (operator instanceof SingleSource) {
            changeSourceNames(newName);
        }
        TreeUtils.updateTreeBelow(this);
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void adjustWidthSize() {
        this.width = Math.max(CellUtils.getCellWidth(jCell), ConstantController.TABLE_CELL_WIDTH);
        String a = alias;
        if (!a.isBlank())
            a = ":" + a;
        String formattedName = this.getType().symbol + a + arguments;
        changeCellName(jCell, formattedName, ConstantController.TABLE_CELL_WIDTH);

    }

    private void changeSourceNames(String newName) {

        List<Column> newColumns = new ArrayList<>();

        for (Column c : columns)
            newColumns.add(Column.changeSourceColumn(c, newName));

        columns = newColumns;

    }
}
