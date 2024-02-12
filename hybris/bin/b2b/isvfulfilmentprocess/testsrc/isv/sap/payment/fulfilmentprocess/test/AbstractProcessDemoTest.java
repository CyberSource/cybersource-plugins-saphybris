package isv.sap.payment.fulfilmentprocess.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.Resource;

import de.hybris.platform.basecommerce.enums.InStockStatus;
import de.hybris.platform.basecommerce.enums.StockLevelUpdateType;
import de.hybris.platform.catalog.CatalogService;
import de.hybris.platform.catalog.model.CatalogModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.product.UnitModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.europe1.model.PriceRowModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.order.OrderService;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.ordersplitting.model.VendorModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.payment.PaymentService;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.processengine.model.BusinessProcessModel;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.product.UnitService;
import de.hybris.platform.servicelayer.ServicelayerTest;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.stock.model.StockLevelHistoryEntryModel;
import org.junit.Before;
import org.junit.Ignore;

import isv.sap.payment.fulfilmentprocess.constants.IsvfulfilmentprocessConstants;

@Ignore
public abstract class AbstractProcessDemoTest extends ServicelayerTest
{
    @Resource
    protected BusinessProcessService businessProcessService;

    @Resource
    protected CatalogService catalogService;

    @Resource
    protected CartService cartService;

    @Resource
    protected ProductService productService;

    @Resource
    protected OrderService orderService;

    @Resource
    protected UserService userService;

    @Resource
    protected ModelService modelService;

    @Resource
    protected I18NService i18nService;

    @Resource
    protected PaymentService paymentService;

    @Resource
    protected UnitService unitService;

    @Before
    public void setUp()
    {
        setupCatalog();
        setupUser();
        setCurrentCatalog();
    }

    protected void setupCatalog()
    {
        final CatalogModel catalog = createCatalog("test");
        final CatalogVersionModel catalogVersion = createCatalogVersion(catalog, "online", Boolean.TRUE);
        final UnitModel pieces = getOrCreateUnit("pieces");
        final CurrencyModel currency = createOrGetCurrency("EUR");
        final VendorModel vendor = createVendor("testVendor");
        final WarehouseModel warehouse1 = createWarehouse("testWarehouse1", vendor);
        final WarehouseModel warehouse2 = createWarehouse("testWarehouse2", vendor);

        for (int i = 1; i <= 10; i++)
        {
            final String code = String.format("product%02d", Integer.valueOf(i));
            final String name = String.format("Product %02d", Integer.valueOf(i));
            final ProductModel product = createProduct(catalogVersion, code, name);

            WarehouseModel warehouse;
            if (i <= 5)
            {
                warehouse = warehouse1;
            }
            else
            {
                warehouse = warehouse2;
            }
            final StockLevelModel stockLevel = this.createStockLevel(product, warehouse, 10);
            modelService.save(stockLevel);

            createPriceRow(currency, Double.valueOf(i), pieces, product, catalogVersion);
        }
        modelService.saveAll();
    }

    private CatalogModel createCatalog(final String id)
    {
        final CatalogModel catalog = new CatalogModel();
        catalog.setId(id);
        modelService.save(catalog);
        return catalog;
    }

    private CatalogVersionModel createCatalogVersion(final CatalogModel catalog, final String version,
            final Boolean active)
    {
        final CatalogVersionModel catalogVersion = new CatalogVersionModel();
        catalogVersion.setCatalog(catalog);
        catalogVersion.setVersion(version);
        catalogVersion.setActive(active);
        modelService.save(catalogVersion);
        return catalogVersion;
    }

    private UnitModel getOrCreateUnit(final String code)
    {
        UnitModel unitItem;
        try
        {
            unitItem = unitService.getUnitForCode(code);
        }
        catch (final UnknownIdentifierException uie)
        {
            unitItem = new UnitModel();
            unitItem.setCode(code);
            unitItem.setUnitType(code);
            modelService.save(unitItem);
        }

        return unitItem;
    }

    private CurrencyModel createOrGetCurrency(final String isoCode)
    {
        try
        {
            return i18nService.getCurrency(isoCode);
        }
        catch (final UnknownIdentifierException e)
        {
            final CurrencyModel currency = modelService.create(CurrencyModel.class);
            currency.setIsocode(isoCode);
            currency.setSymbol(isoCode);
            currency.setBase(Boolean.TRUE);
            currency.setActive(Boolean.TRUE);
            currency.setConversion(Double.valueOf(1));
            modelService.save(currency);
            return currency;
        }
    }

    private VendorModel createVendor(final String code)
    {
        final VendorModel vendor = new VendorModel();
        vendor.setCode(code);
        modelService.save(vendor);
        return vendor;
    }

    private WarehouseModel createWarehouse(final String code, final VendorModel vendor)
    {
        final WarehouseModel warehouse = new WarehouseModel();
        warehouse.setCode(code);
        warehouse.setVendor(vendor);
        modelService.save(warehouse);
        return warehouse;
    }

    private ProductModel createProduct(final CatalogVersionModel catalogVersion, final String code, final String name)
    {
        final ProductModel product = modelService.create(ProductModel.class);
        product.setCatalogVersion(catalogVersion);
        product.setCode(code);
        product.setName(name);
        modelService.save(product);
        return product;
    }

    private PriceRowModel createPriceRow(final CurrencyModel currency, final Double price, final UnitModel unit,
            final ProductModel product, final CatalogVersionModel catalogVersion)
    {
        final PriceRowModel priceRow = new PriceRowModel();
        priceRow.setCurrency(currency);
        priceRow.setPrice(price);
        priceRow.setUnit(unit);
        priceRow.setNet(Boolean.TRUE);
        priceRow.setProduct(product);
        priceRow.setCatalogVersion(catalogVersion);
        modelService.save(priceRow);

        return priceRow;
    }

    protected void setCurrentCatalog()
    {
        catalogService.setSessionCatalogVersion("test", "online");
    }

    protected void setupUser(final String uId, final String name)
    {
        final UserModel user = new UserModel();
        user.setUid(uId);
        user.setName(name);
        modelService.save(user);
        userService.setCurrentUser(user);
    }

    protected void setupUser()
    {
        setupUser("testUser", null);
    }

    protected BusinessProcessModel createProcess(final OrderModel order)
    {
        final OrderProcessModel process = businessProcessService.createProcess(
                "test" + System.currentTimeMillis(), IsvfulfilmentprocessConstants.ORDER_PROCESS_NAME);
        process.setOrder(order);
        modelService.saveAll(process, order);
        return process;
    }

    protected StockLevelModel createStockLevel(final ProductModel product, final WarehouseModel warehouse,
            final int available)
    {
        final StockLevelModel stockLevel = this
                .createStockLevel(product, warehouse, available, 0, 0, InStockStatus.NOTSPECIFIED,
                        -1, true);
        return stockLevel;
    }

    protected StockLevelModel createStockLevel(final ProductModel product, final WarehouseModel warehouse,
            final int available,
            final int overSelling, final int reserved, final InStockStatus status, final int maxStockLevelHistoryCount,
            final boolean treatNegativeAsZero)
    {
        final StockLevelModel stockLevel = modelService.create(StockLevelModel.class);
        stockLevel.setProductCode(product.getCode());
        stockLevel.setWarehouse(warehouse);
        stockLevel.setAvailable(available);
        stockLevel.setOverSelling(overSelling);
        stockLevel.setReserved(reserved);
        stockLevel.setInStockStatus(status);
        stockLevel.setMaxStockLevelHistoryCount(maxStockLevelHistoryCount);
        stockLevel.setTreatNegativeAsZero(treatNegativeAsZero);

        final List<StockLevelHistoryEntryModel> historyEntries = new ArrayList<StockLevelHistoryEntryModel>();
        final StockLevelHistoryEntryModel entry = this.createStockLevelHistoryEntry(stockLevel, available, 0,
                StockLevelUpdateType.WAREHOUSE, "new in stock");
        historyEntries.add(entry);
        stockLevel.setStockLevelHistoryEntries(historyEntries);
        modelService.save(stockLevel);
        return stockLevel;
    }

    protected StockLevelHistoryEntryModel createStockLevelHistoryEntry(final StockLevelModel stockLevel,
            final int actual,
            final int reserved, final StockLevelUpdateType updateType, final String comment)
    {
        final StockLevelHistoryEntryModel historyEntry = modelService.create(StockLevelHistoryEntryModel.class);
        historyEntry.setStockLevel(stockLevel);
        historyEntry.setActual(actual);
        historyEntry.setReserved(reserved);
        historyEntry.setUpdateType(updateType);
        if (comment != null)
        {
            historyEntry.setComment(comment);
        }
        historyEntry.setUpdateDate(new Date());
        modelService.save(historyEntry);
        return historyEntry;
    }
}
