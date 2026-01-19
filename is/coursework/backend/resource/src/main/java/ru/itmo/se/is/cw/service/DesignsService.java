package ru.itmo.se.is.cw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.se.is.cw.dto.ProductDesignRequestDto;
import ru.itmo.se.is.cw.dto.ProductDesignResponseDto;
import ru.itmo.se.is.cw.dto.RequiredMaterialDto;
import ru.itmo.se.is.cw.dto.filter.ProductDesignFilter;
import ru.itmo.se.is.cw.dto.specification.ProductDesignSpecification;
import ru.itmo.se.is.cw.exception.EntityNotFoundException;
import ru.itmo.se.is.cw.mapper.ProductDesignFileMapper;
import ru.itmo.se.is.cw.mapper.ProductDesignMapper;
import ru.itmo.se.is.cw.mapper.RequiredMaterialMapper;
import ru.itmo.se.is.cw.model.ProductDesignEntity;
import ru.itmo.se.is.cw.repository.ProductDesignRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DesignsService {

    private final ProductDesignMapper productDesignMapper;
    private final ProductDesignRepository productDesignRepository;
    private final MaterialsService materialsService;
    private final RequiredMaterialMapper requiredMaterialMapper;
    private final FilesService filesService;
    private final ProductDesignFileMapper productDesignFileMapper;
    private final EmployeesService employeesService;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public ProductDesignResponseDto getDesignById(Long id) {
        return productDesignMapper.toDto(getById(id));
    }

    @Transactional
    public ProductDesignResponseDto createDesign(ProductDesignRequestDto request) {
        ProductDesignEntity design = productDesignMapper.toEntity(request);
        applyFiles(design, request.getFileIds());
        applyRequiredMaterials(design, request.getRequiredMaterials());
        design.setConstructor(
                employeesService.getByAccountId(currentUserService.getAccountId())
        );
        return productDesignMapper.toDto(
                productDesignRepository.save(design)
        );
    }

    @Transactional
    public ProductDesignEntity createEmptyDesign() {
        ProductDesignEntity design = new ProductDesignEntity();
        design.setProductName(UUID.randomUUID().toString());
        return productDesignRepository.save(design);
    }

    @Transactional
    public ProductDesignResponseDto updateDesign(Long id, ProductDesignRequestDto request) {
        ProductDesignEntity design = getById(id);
        productDesignMapper.updateEntity(design, request);
        applyFiles(design, request.getFileIds());
        applyRequiredMaterials(design, request.getRequiredMaterials());
        design.setConstructor(
                employeesService.getByAccountId(currentUserService.getAccountId())
        );
        return productDesignMapper.toDto(
                productDesignRepository.save(design)
        );
    }

    @Transactional
    public void deleteDesign(Long id) {
        productDesignRepository.delete(getById(id));
    }

    @Transactional(readOnly = true)
    public Page<ProductDesignResponseDto> getDesigns(Pageable pageable, ProductDesignFilter filter) {
        return productDesignRepository
                .findAll(ProductDesignSpecification.byFilter(filter), pageable)
                .map(productDesignMapper::toDto);
    }

    @Transactional(readOnly = true)
    public ProductDesignEntity getById(Long id) {
        return productDesignRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product design with id " + id + " not found"));
    }

    private void applyFiles(ProductDesignEntity design, List<Long> fileIds) {
        if (fileIds == null) {
            return;
        }

        design.setFiles(
                fileIds.stream()
                        .map(filesService::getById)
                        .map(file ->
                                productDesignFileMapper.toEntity(
                                        design,
                                        file
                                )
                        )
                        .toList()
        );
    }

    private void applyRequiredMaterials(ProductDesignEntity design, List<RequiredMaterialDto> requiredMaterials) {
        if (requiredMaterials == null) {
            return;
        }

        design.getRequiredMaterials().clear();

        requiredMaterials.stream()
                .map(dto ->
                        requiredMaterialMapper.toEntity(
                                dto,
                                materialsService.getById(dto.getMaterialId()),
                                design
                        )
                )
                .forEach(design::addMaterial);
    }

    @Transactional
    public ProductDesignResponseDto addFileToDesign(Long designId, Long fileId) {
        ProductDesignEntity design = getById(designId);
        var file = filesService.getById(fileId);
        var designFile = productDesignFileMapper.toEntity(design, file);
        design.addFile(designFile);
        return productDesignMapper.toDto(
                productDesignRepository.save(design)
        );
    }

    @Transactional
    public ProductDesignResponseDto addMaterialToDesign(Long designId, RequiredMaterialDto materialDto) {
        ProductDesignEntity design = getById(designId);
        var material = materialsService.getById(materialDto.getMaterialId());
        var requiredMaterial = requiredMaterialMapper.toEntity(materialDto, material, design);
        design.addMaterial(requiredMaterial);
        return productDesignMapper.toDto(
                productDesignRepository.save(design)
        );
    }

    @Transactional
    public ProductDesignResponseDto updateDesignMaterials(Long designId, List<RequiredMaterialDto> materials) {
        ProductDesignEntity design = getById(designId);

        design.getRequiredMaterials().clear();

        applyRequiredMaterials(design, materials);

        return productDesignMapper.toDto(
                productDesignRepository.save(design)
        );
    }

    @Transactional
    public ProductDesignResponseDto assignDesigner(Long designId) {
        ProductDesignEntity design = getById(designId);
        design.setConstructor(
                employeesService.getByAccountId(currentUserService.getAccountId())
        );
        return productDesignMapper.toDto(
                productDesignRepository.save(design)
        );
    }

    @Transactional
    public ProductDesignResponseDto removeFileFromDesign(Long designId, Long fileId) {
        ProductDesignEntity design = getById(designId);
        design.getFiles().removeIf(designFile -> designFile.getFile().getId().equals(fileId));
        return productDesignMapper.toDto(
                productDesignRepository.save(design)
        );
    }

    @Transactional
    public ProductDesignEntity copyDesign(ProductDesignEntity source) {
        ProductDesignEntity copy = new ProductDesignEntity();
        copy.setProductName(source.getProductName());

        copy = productDesignRepository.save(copy);

        for (var sourceFile : source.getFiles()) {
            var copyFile = productDesignFileMapper.toEntity(copy, sourceFile.getFile());
            copy.addFile(copyFile);
        }

        for (var sourceMaterial : source.getRequiredMaterials()) {
            var copyMaterial = requiredMaterialMapper.toEntity(
                    new RequiredMaterialDto(
                            sourceMaterial.getMaterial().getId(),
                            sourceMaterial.getAmount().doubleValue()
                    ),
                    sourceMaterial.getMaterial(),
                    copy
            );
            copy.addMaterial(copyMaterial);
        }

        return productDesignRepository.save(copy);
    }
}

