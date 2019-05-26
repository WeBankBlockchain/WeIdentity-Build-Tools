package com.webank.weid.command;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;

import lombok.Getter;

/**
 * command line parameter parser.
 * @author tonychen 2019年6月4日
 *
 */
@Getter
public class CommandArgs {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	@Parameter
	private List<String> parameters = new ArrayList<>();

	@Parameter(names = { "--weid" }, description = "weidentity did")
	private String weid;

	@Parameter(names = "--cpt-dir", description = "the directory which the cpt json files will be found and be registered to blockchain.")
	private String cptDir;

	@Parameter(names = "--private-key", description = "private key file")
	private String privateKey;
	
	@Parameter(names = "--cpt-list", description = "cpt id list")
	private String cptIdList;
	
	@Parameter(names = "--cpt-id", description = "cpt id")
	private String cptId;
	
	@Parameter(names = "--cpt-file", description = "cpt file")
	private String cptFile;
	
	@Parameter(names = "--org-name", description = "org name")
	private String orgName;

	@Parameter(names = "--type", description = "type")
	private String type;
}
