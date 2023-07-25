package one.modality.ecommerce.client2018.controls.bookingoptionspanel;

import javafx.scene.layout.BorderPane;
import one.modality.ecommerce.client2018.businessdata.workingdocument.WorkingDocument;
import one.modality.event.client2018.controls.sectionpanel.SectionPanelFactory;
import one.modality.hotel.shared2018.businessdata.time.DaysArray;
import one.modality.hotel.shared2018.businessdata.time.DaysArrayBuilder;
import one.modality.ecommerce.client2018.businessdata.workingdocument.WorkingDocumentLine;
import one.modality.base.client.util.functions.TranslateFunction;
import one.modality.base.shared.entities.formatters.EventPriceFormatter;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.entities.Item;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.orm.expression.Expression;
import dev.webfx.stack.orm.expression.lci.DomainReader;
import dev.webfx.stack.orm.expression.terms.function.AggregateFunction;
import dev.webfx.stack.orm.entity.EntityList;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.EntitiesToVisualResultMapper;
import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.extras.visual.controls.grid.SkinnedVisualGrid;
import dev.webfx.extras.visual.VisualResult;
import dev.webfx.extras.visual.SelectionMode;
import dev.webfx.extras.type.PrimType;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.util.Objects;
import dev.webfx.platform.util.collection.Collections;

import java.util.List;

import static dev.webfx.stack.orm.domainmodel.formatter.FormatterRegistry.registerFormatter;

/**
 * @author Bruno Salmon
 */
public final class BookingOptionsPanel {

    private final VisualGrid visualGrid;
    private BorderPane optionsPanel;
    private EntityList<DocumentLine> lineEntities;

    public BookingOptionsPanel() {
        visualGrid = new SkinnedVisualGrid(); // LayoutUtil.setMinMaxHeightToPref(new DataGrid());
        visualGrid.setHeaderVisible(false);
        visualGrid.setFullHeight(true);
        visualGrid.setSelectionMode(SelectionMode.DISABLED);
        new AggregateFunction<DocumentLine>("days_agg", PrimType.STRING) {
            @Override
            public Object evaluateOnAggregates(DocumentLine referrer, Object[] aggregates, Expression<DocumentLine> operand, DomainReader<DocumentLine> domainReader) {
                DaysArrayBuilder daysArrayBuilder = new DaysArrayBuilder();
                for (Object dl : aggregates) {
                    DaysArray daysArray = (DaysArray) ((DocumentLine) dl).getFieldValue("daysArray");
                    daysArrayBuilder.addSeries(daysArray.toSeries(), null);
                }
                return daysArrayBuilder.build().toSeries().toText("dd/MM");
            }
        }.register();
        new TranslateFunction().register();
        FXProperties.runOnPropertiesChange(this::updateGrid, I18n.dictionaryProperty());
    }

    public void syncUiFromModel(WorkingDocument workingDocument) {
        registerFormatter("priceWithCurrency", new EventPriceFormatter(workingDocument.getEventAggregate().getEvent()));
        workingDocument.getComputedPrice(); // ensuring the price has been computed
        //Doesn't work on Android: syncUiFromModel(workingDocument.getWorkingDocumentLines().stream().map(BookingOptionsPanel::createDocumentLine).filter(Objects::nonNull).collect(Collectors.toList()), workingDocument.getDocument().getStore());
        syncUiFromModel(Collections.mapFilter(workingDocument.getWorkingDocumentLines(), BookingOptionsPanel::createDocumentLine, Objects::nonNull), workingDocument.getDocument().getStore());
    }

    public void syncUiFromModel(List<DocumentLine> documentLines, EntityStore entityStore) {
        syncUiFromModel(EntityList.create("bookingOptions", entityStore, documentLines));
    }

    public void syncUiFromModel(EntityList<DocumentLine> lineEntities) {
        this.lineEntities = lineEntities;
        updateGrid();
    }

    private void updateGrid() {
        if (lineEntities != null) {
            VisualResult rs = generateGroupedLinesResult();
            visualGrid.setVisualResult(rs);
        }
    }

    private VisualResult generateDetailedLinesResult() {
        return EntitiesToVisualResultMapper.selectAndMapEntitiesToVisualResult(lineEntities,
                "select [" +
                        "'item.icon'," +
                        "'translate(item)'," +
                        "{expression: 'dates', textAlign: 'center'}," +
                        "{expression: 'price_net', format: 'priceWithCurrency'}" +
                        "] from DocumentLine where dates<>'' order by item.family.ord,item.name");
    }

    private VisualResult generateGroupedLinesResult() {
        return EntitiesToVisualResultMapper.selectAndMapEntitiesToVisualResult(lineEntities,
                "select [" +
                        // Displaying the actual item if only one is present for the item family, otherwise just displaying the item family (without further details)
                        "'item.family.icon, sum(1) != 1 ? translate(item.family) : string_agg(translate(item), `, ` order by item.name)'," +
                        "{expression: 'days_agg()', textAlign: 'center'}," +
                        "{expression: 'sum(price_net)', format: 'priceWithCurrency'}" +
                        "] from DocumentLine where dates<>'' group by item.family order by item.family.ord");
    }

    public VisualGrid getGrid() {
        return visualGrid;
    }

    public BorderPane getOptionsPanel() {
        if (optionsPanel == null) {
            optionsPanel = SectionPanelFactory.createSectionPanel("YourOptions");
            optionsPanel.setCenter(getGrid());
        }
        return optionsPanel;
    }

    private static DocumentLine createDocumentLine(WorkingDocumentLine wdl) {
        Item item = wdl.getItem();
        EntityStore store = item.getStore();
        DocumentLine dl = store.createEntity(DocumentLine.class);
        dl.setSite(wdl.getSite());
        dl.setItem(item);
        dl.setFieldValue("price_net", wdl.getPrice());
        dl.setFieldValue("daysArray", wdl.getDaysArray());
        dl.setFieldValue("dates", wdl.getDaysArray().toSeries().toText("dd/MM"));
        return dl;
    }
}
