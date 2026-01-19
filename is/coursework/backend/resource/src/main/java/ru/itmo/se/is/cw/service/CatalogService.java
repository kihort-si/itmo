package ru.itmo.se.is.cw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.se.is.cw.dto.ProductCatalogRequestDto;
import ru.itmo.se.is.cw.dto.ProductCatalogResponseDto;
import ru.itmo.se.is.cw.dto.filter.ProductCatalogFilter;
import ru.itmo.se.is.cw.dto.specification.ProductCatalogSpecification;
import ru.itmo.se.is.cw.exception.EntityNotFoundException;
import ru.itmo.se.is.cw.mapper.ProductCatalogMapper;
import ru.itmo.se.is.cw.mapper.ProductPhotoMapper;
import ru.itmo.se.is.cw.model.ProductCatalogEntity;
import ru.itmo.se.is.cw.model.ProductDesignEntity;
import ru.itmo.se.is.cw.repository.ProductCatalogRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final ProductCatalogRepository productCatalogRepository;
    private final ProductCatalogMapper productCatalogMapper;
    private final DesignsService designsService;
    private final ProductPhotoMapper productPhotoMapper;
    private final FilesService filesService;

    @Transactional(readOnly = true)
    public Page<ProductCatalogResponseDto> getProducts(Pageable pageable, ProductCatalogFilter filter) {
        return productCatalogRepository
                .findAll(ProductCatalogSpecification.byFilter(filter), pageable)
                .map(productCatalogMapper::toDto);
    }

    @Transactional
    public ProductCatalogResponseDto createProduct(ProductCatalogRequestDto request) {
        ProductCatalogEntity entity = productCatalogMapper.toEntity(request);
        applyProductDesign(entity, request.getProductDesignId());
        applyProductPhotos(entity, request.getPhotoFileIds());
        return productCatalogMapper.toDto(
                productCatalogRepository.save(entity)
        );
    }

    @Transactional(readOnly = true)
    public ProductCatalogResponseDto getProductById(Long id) {
        return productCatalogMapper.toDto(getById(id));
    }

    @Transactional(readOnly = true)
    public ProductCatalogEntity getById(Long id) {
        return productCatalogRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product with id " + id + " not found"));
    }

    @Transactional(readOnly = true)
    public ProductCatalogEntity getByProductDesignId(Long productDesignId) {
        ProductCatalogFilter filter = new ProductCatalogFilter();
        filter.setProductDesignId(productDesignId);
        return productCatalogRepository
                .findAll(ProductCatalogSpecification.byFilter(filter))
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Transactional
    public ProductCatalogResponseDto updateProduct(Long id, ProductCatalogRequestDto request) {
        ProductCatalogEntity entity = getById(id);
        applyProductDesign(entity, request.getProductDesignId());
        applyProductPhotos(entity, request.getPhotoFileIds());
        productCatalogMapper.updateEntity(entity, request);
        return productCatalogMapper.toDto(
                productCatalogRepository.save(entity)
        );
    }

    @Transactional
    public void deleteProduct(Long id) {
        productCatalogRepository.delete(getById(id));
    }

    private void applyProductDesign(ProductCatalogEntity entity, Long productDesignId) {
        ProductDesignEntity design = designsService.getById(productDesignId);
        entity.setProductDesign(design);
    }

    private void applyProductPhotos(ProductCatalogEntity entity, List<Long> fileIds) {
        entity.setPhotos(
                fileIds.stream()
                        .map(filesService::getById)
                        .map(file -> productPhotoMapper.toEntity(
                                file,
                                entity
                        ))
                        .toList()
        );
    }
}

